package com.etester.data.domain.util.cache;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.etester.data.domain.content.instance.Testinstance;
import com.etester.data.domain.test.SerializedTestinstance;
import com.etester.data.domain.util.compress.DataCompressor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.DatatypeConverter;

public class EtesterTestinstanceJdbcCacheClient extends NamedParameterJdbcDaoSupport {
	
	// Serialized Testinstance Stuff - used for caching
    public static final String findSerializedTestinstanceByTestinstanceId = 
    		"SELECT st.* FROM serialized_testinstance st WHERE st.id_testinstance = :idTestinstance ";
	public static final String insertSerializedTestinstanceSQL = "INSERT INTO serialized_testinstance (id_testinstance, testinstance_string_json, date_saved) "
			+ " VALUES (:idTestinstance, :testinstanceStringJson, :dateSaved)";

	private DataCompressor dataCompressor;

	/**
	 * @return the dataCompressor
	 */
	public DataCompressor getDataCompressor() {
		return dataCompressor;
	}

	/**
	 * @param dataCompressor the dataCompressor to set
	 */
	public void setDataCompressor(DataCompressor dataCompressor) {
		this.dataCompressor = dataCompressor;
	}

	public static void main(String[] args) {
	}


	/**
	 * Locate and return a Testinstance from the redis database
	 * @param idTestinstance
	 * @return
	 */
	public Testinstance locateTestinstance(Long idTestinstance) {
        Testinstance deserializedTestinstance = null;
		String sql = findSerializedTestinstanceByTestinstanceId;
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("idTestinstance", idTestinstance);
		// first try to locate a SerializedTestinstance that may already exist in the system
        BeanPropertyRowMapper<SerializedTestinstance> serializedtestinstanceRowMapper = BeanPropertyRowMapper.newInstance(SerializedTestinstance.class);
        SerializedTestinstance serializedTestinstance = null;
        try {
        	serializedTestinstance = getNamedParameterJdbcTemplate().queryForObject(sql, args, serializedtestinstanceRowMapper);
    		System.out.println("Found JSON in Database for TestInstance: " + idTestinstance);
        	// massage the published and public attributes
        } catch (IncorrectResultSizeDataAccessException e) {}
        if (serializedTestinstance == null) {
        	return null;
        } 
        
        // reconstitute the test and return it.
        String completeTestinstanceStringJson = serializedTestinstance.getTestinstanceStringJson();
        
        // uncompress the string - note that this is Binary data.  should be compressed Base64 
		// Base64 is in java 8 --- java.util.Base64
		//	String uncompressedTestinstanceStringJson = dataCompressor.decompressToString(Base64.getDecoder().decode(completeTestinstanceStringJson));
		// 	For Java 6 & 7
		String uncompressedTestinstanceStringJson = dataCompressor.decompressToString(DatatypeConverter.parseBase64Binary(completeTestinstanceStringJson));
        
        ObjectMapper mapper = new ObjectMapper();
        try {
        	deserializedTestinstance =  mapper.readValue(uncompressedTestinstanceStringJson, Testinstance.class);
        } catch (JsonGenerationException e) {
           e.printStackTrace();
        } catch (JsonMappingException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        }
        
        return deserializedTestinstance;
	}

	/**
	 * Locate and return a Testinstance from the redis database
	 * @param idTestinstance
	 * @return
	 */
	public boolean storeTestinstance(Testinstance testinstance) {
        String completeTestinstanceStringJson = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			completeTestinstanceStringJson = mapper.writeValueAsString(testinstance);
			System.out.println("Newly Minted: " + completeTestinstanceStringJson);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// compress it - note that this is Binary data.  should be compressed Base64 
		// Base64 is in java 8 --- java.util.Base64
		//	String compressedTestinstanceStringJson = Base64.encode(dataCompressor.compress(completeTestinstanceStringJson));
		// 	For Java 6 & 7
		String compressedTestinstanceStringJson = DatatypeConverter.printBase64Binary(dataCompressor.compress(completeTestinstanceStringJson));
		
		SerializedTestinstance serializedTestinstance = new SerializedTestinstance();
		serializedTestinstance.setIdTestinstance(testinstance.getIdTestinstance());
//		serializedTestinstance.setTestinstanceStringJson(completeTestinstanceStringJson);
		serializedTestinstance.setTestinstanceStringJson(compressedTestinstanceStringJson);
		serializedTestinstance.setDateSaved(new Date());
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(serializedTestinstance);
        try {
        	getNamedParameterJdbcTemplate().update(insertSerializedTestinstanceSQL, parameterSource);
        } catch (DataAccessException dae) {
        	dae.printStackTrace();
        	return false;
        }
		return true;
	}



}

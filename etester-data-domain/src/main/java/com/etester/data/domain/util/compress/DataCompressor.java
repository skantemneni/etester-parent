package com.etester.data.domain.util.compress;

public interface DataCompressor {

	public byte[] compress(byte[] bytesToCompress);

	public byte[] compress(String stringToCompress);

	public byte[] decompress(byte[] bytesToDecompress);

	public String decompressToString(byte[] bytesToDecompress);

}
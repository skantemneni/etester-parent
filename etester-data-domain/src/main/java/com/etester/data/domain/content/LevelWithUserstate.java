package com.etester.data.domain.content;

import java.util.List;

import com.etester.data.domain.content.core.Level;
import com.etester.data.domain.content.instance.Practiceinstance;

import lombok.Data;

@Data
public class LevelWithUserstate {

    private Long idLevel;
	
    private Level level;
	
    List<Practiceinstance> skillPracticeinstanceList;

}

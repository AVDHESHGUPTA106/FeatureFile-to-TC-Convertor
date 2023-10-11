package com.lex.FeatureClassification.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitUserDetail {

	private String userName;
	private String commitId;
	private String comment;

}

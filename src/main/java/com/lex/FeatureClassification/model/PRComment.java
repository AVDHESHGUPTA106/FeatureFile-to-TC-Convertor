package com.lex.FeatureClassification.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PRComment {
	private String prComment;
	private String userName;
	private String creationDate;
	private boolean isRvwComment;
}

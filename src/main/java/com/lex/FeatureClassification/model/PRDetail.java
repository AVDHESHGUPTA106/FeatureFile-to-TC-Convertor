package com.lex.FeatureClassification.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PRDetail {
	private String prUserName;
	private String repoName;
	private Long prNumber;
	private String prLink;
	private String prTitle;
	private String prCreationDate;
	private String prAgeing;
	private String prFrmBranchName;
	private String prToBranchName;
	private String prStatus;
	private String prDescription;
	private String prComments;
	private boolean isParent;
	private List<PRComment> prCommentList;

}

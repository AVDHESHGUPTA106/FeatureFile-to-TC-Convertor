package com.lex.FeatureClassification.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
public class GitSheetModel {
	
	@JsonProperty("Git_Repo_Url")
	private String gitRepoUrl;
	@JsonProperty("Git_Access_Token")
	private String gitAccessToken;

}

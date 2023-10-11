package com.lex.FeatureClassification.model;

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
public class UrlDTO {
	private String hostUrl;
    private String ownerRepoPart;
    private String apiUrl;
}

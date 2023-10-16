package com.lex.FeatureClassification.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.springframework.format.annotation.DateTimeFormat;

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
public class RemoteRequest {
	@NotEmpty(message = "Git URL Cant be Empty")
	private String url;
	@NotEmpty(message = "Access Token Cant be Empty")
	private String password;
	@DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateFrom;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dateTo;
}

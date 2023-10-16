package com.lex.FeatureClassification.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lex.FeatureClassification.model.RemoteRequest;
import com.lex.FeatureClassification.util.UrlUtils;

@Service
public class ValidateService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	public Object validateNGetResponseFrmGitUrl(RemoteRequest remoteRequest) throws URISyntaxException {
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + remoteRequest.getPassword()); // accessToken can be the secret key you generate.
		RequestEntity<Void> reqEntity = RequestEntity.get(new URI(UrlUtils.getPrUrl(remoteRequest))).headers(headers).build();
		ResponseEntity<String> response = restTemplate.exchange(reqEntity, String.class);
	
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			return "Error: Invalid Git Access Token";
		}
		if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
			return String.format("Error: Git Repository Not Found %s",remoteRequest.getUrl());
		}
		return response;
	}

	public static boolean isValidGITRepo(String str) {
		System.out.println(str);
		// Regex to check valid GIT Repository
		String regex = "((http|git|ssh|http(s)|file|\\/?)|" + "(git@[\\w\\.]+))(:(\\/\\/)?)"
				+ "([\\w\\.@\\:/\\-~]+)(\\.git)(\\/)?";

		// Compile the ReGex
		Pattern p = Pattern.compile(regex);

		// If the str is empty return false
		if (str == null) {
			return false;
		}

		// Pattern class contains matcher()
		// method to find matching between
		// given str using regex.
		Matcher m = p.matcher(str);

		// Return if the str
		// matched the ReGex
		return m.matches();
	}

}

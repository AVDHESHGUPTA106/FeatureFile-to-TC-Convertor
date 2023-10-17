package com.lex.FeatureClassification.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lex.FeatureClassification.model.GitSheetModel;
import com.lex.FeatureClassification.model.PRDetail;
import com.lex.FeatureClassification.model.RemoteRequest;
import com.lex.FeatureClassification.service.ReportGeneratorService;
import com.lex.FeatureClassification.service.ValidateService;

@Controller
public class FeatureClassificationController {

	static Logger log = LoggerFactory.getLogger(FeatureClassificationController.class);

	@Autowired
	ReportGeneratorService reportGeneratorService;
	
	@Autowired
	ValidateService validateService;

	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("remoteRequest", new RemoteRequest());
		return "index";
	}

	/**
	 * 
	 * @param remoteRequest
	 * @param result
	 * @param model
	 * @return
	 */
	@PostMapping("/genprreport")
	public Object generatePrExcelReport(@Valid @ModelAttribute(name = "remoteRequest") RemoteRequest remoteRequest,
			BindingResult result, Model model) {
		try {

			ObjectMapper objectMapper = new ObjectMapper();
			TypeReference<List<GitSheetModel>> listType = new TypeReference<List<GitSheetModel>>() {};
			List<GitSheetModel> gitModelList = objectMapper.readValue(remoteRequest.getRepolist(), listType);
			if(gitModelList.size() == 0) {
				gitModelList = List.of(GitSheetModel.builder()
						.gitRepoUrl(remoteRequest.getUrl())
						.gitAccessToken(remoteRequest.getPassword()).build());
			}
			List<GitSheetModel> invalidGitModel = gitModelList.stream().filter(gsm -> !ValidateService.isValidGITRepo(gsm.getGitRepoUrl()))
					.collect(Collectors.toList());
			if (invalidGitModel.size() != 0) {
				List<String> invalidGitUrl = invalidGitModel.stream().map(gsm->gsm.getGitRepoUrl()).collect(Collectors.toList());
				ObjectError error = new ObjectError("globalError",
						String.format("Error: Invalid Git Repository %s", invalidGitUrl.toString()));
				result.addError(error);
			} else {
				List<PRDetail> prDetails = new ArrayList<>();
				gitModelList.forEach(gsm ->{
					try {
						remoteRequest.setUrl(gsm.getGitRepoUrl());
						remoteRequest.setPassword(gsm.getGitAccessToken());
						Object responseFrmGitUrl = validateService.validateNGetResponseFrmGitUrl(remoteRequest);
						if (responseFrmGitUrl instanceof String && !responseFrmGitUrl.toString().isEmpty()) {
							ObjectError error = new ObjectError("globalError", "Error: " + responseFrmGitUrl.toString());
							result.addError(error);
						}else if(!result.hasErrors()&& responseFrmGitUrl instanceof ResponseEntity) {
							prDetails.addAll(reportGeneratorService.getPRDetails(remoteRequest, responseFrmGitUrl));
						}
					} catch (URISyntaxException e) {
						ObjectError error = new ObjectError("globalError", "Error: " + e.getMessage());
						result.addError(error);
					}
				});
				if (prDetails.size() == 0) {
					ObjectError error = new ObjectError("globalError",
							String.format("Info: PR comment(s) are not found for selected date range %s - %s", remoteRequest.getDateFrom(), remoteRequest.getDateTo()));
					result.addError(error);
				} else return reportGeneratorService.generatePRExcelSheet(prDetails);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			ObjectError error = new ObjectError("globalError", "Error: " + e.getMessage());
			result.addError(error);
		}
		return "index";

	}

}

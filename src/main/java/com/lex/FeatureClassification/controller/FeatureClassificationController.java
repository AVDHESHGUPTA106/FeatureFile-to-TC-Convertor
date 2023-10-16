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

			List<String> gitUrlList = List.of(remoteRequest.getUrl().split(";"));
			List<String> inValidGitUrl = gitUrlList.stream().filter(url -> !ValidateService.isValidGITRepo(url))
					.collect(Collectors.toList());
			if (inValidGitUrl.size() != 0) {
				ObjectError error = new ObjectError("globalError",
						String.format("Error: Invalid Git Repository %s", inValidGitUrl.toString()));
				result.addError(error);
			} else {
				List<PRDetail> prDetails = new ArrayList<>();
				gitUrlList.forEach(url ->{
					try {
						remoteRequest.setUrl(url);
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

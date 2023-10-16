package com.lex.FeatureClassification.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.lex.FeatureClassification.model.PRComment;
import com.lex.FeatureClassification.model.PRDetail;
import com.lex.FeatureClassification.model.PRDetail.PRDetailBuilder;
import com.lex.FeatureClassification.model.RemoteRequest;
import com.lex.FeatureClassification.util.GenerateExcel;

@Service
public class ReportGeneratorService {
	
	static Logger log = LoggerFactory.getLogger(ReportGeneratorService.class);
	
	private DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
	
	private String initialComment = StringUtils.EMPTY;

	private String createdAt = StringUtils.EMPTY;

	private String prAgeing = StringUtils.EMPTY;

	private ZonedDateTime closedDate = null;

	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * 
	 * @param prDetails
	 * @return
	 * @throws URISyntaxException
	 */
	public ResponseEntity<Resource> generatePRExcelSheet(List<PRDetail> prDetails) throws URISyntaxException {
		ByteArrayInputStream inputStream = GenerateExcel.generateExcel(prDetails);
		Resource resource = new InputStreamResource(inputStream);
		HttpCookie cookie = ResponseCookie.from("downloadID", "GitPRReport").build();
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=GitPRReport.xlsx");
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).headers(headers).body(resource);
	}

	/**
	 * 
	 * @param accessToken
	 * @param urlDTO
	 * @param responseFrmGitUrl
	 * @return
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("unchecked")
	public List<PRDetail> getPRDetails(RemoteRequest remoteRequest, Object responseFrmGitUrl)
			throws URISyntaxException {

		List<PRDetail> prDetails = new ArrayList<>();

		String result = ((ResponseEntity<String>) responseFrmGitUrl).getBody();
		JSONObject jsonResult = new JSONObject(result);
		JSONArray jsonArray = (JSONArray) (jsonResult.get("items"));

		for (int i = 0; i < jsonArray.length(); i++) {

			initialComment = StringUtils.EMPTY;
			createdAt = StringUtils.EMPTY;
			prAgeing = StringUtils.EMPTY;
			closedDate = null;

			JSONObject obj = (JSONObject) (jsonArray.getJSONObject(i));
			String timelineUrl = obj.getString("timeline_url");
			String repoUrl = obj.getString("repository_url");
			String repoName = repoUrl.substring(repoUrl.lastIndexOf("/") + 1);
			String pullsUrl = obj.getString("url").replace("issues", "pulls");
			String commentsUrl = obj.getString("comments_url").replace("issues", "pulls");

			List<PRComment> prCommentList = getPRComments(commentsUrl, remoteRequest.getPassword());

			if (StringUtils.equalsIgnoreCase("open", obj.getString("state"))) {
				closedDate = ZonedDateTime.now();
			}
			List<PRComment> prCommitCommentList = getPRTimelineComments(timelineUrl, remoteRequest.getPassword(),
					obj.getString("title"));

			Comparator<PRComment> comparator = (c1, c2) -> {
				return ZonedDateTime.parse(c1.getCreationDate(), formatter.withZone(ZoneId.of("UTC"))).toLocalDateTime()
						.compareTo(ZonedDateTime.parse(c2.getCreationDate(), formatter.withZone(ZoneId.of("UTC")))
								.toLocalDateTime());
			};

			List<PRComment> prCombinedCommentList = Stream.concat(prCommentList.stream(), prCommitCommentList.stream())
					.sorted(comparator).collect(Collectors.toList());

			PRDetailBuilder prDetailBuilder = PRDetail.builder()
					.prUserName(obj.getJSONObject("user").getString("login")).repoName(repoName)
					.prDescription(!"null".equals(ObjectUtils.defaultIfNull(obj.get("body"), "").toString())?obj.getString("body"):StringUtils.EMPTY).prLink(obj.getString("html_url"))
					.prTitle(obj.getString("title")).prCreationDate(obj.getString("created_at"))
					.prStatus(obj.getString("state")).prComments(initialComment).prAgeing(prAgeing)
					.prCommentList(prCombinedCommentList).isParent(true);

			JSONObject prJsonObject = new JSONObject(getPrDetailsData(pullsUrl, remoteRequest.getPassword()));

			JSONObject headJsonObject = prJsonObject.getJSONObject("head");
			Object repoObject = headJsonObject.get("repo");
			if (ObjectUtils.isNotEmpty(repoObject) && repoObject instanceof JSONObject) {
				prDetailBuilder.prFrmBranchName(String.valueOf(headJsonObject.get("ref")));
				prDetailBuilder.prToBranchName(headJsonObject.getJSONObject("repo").getString("default_branch"));
			}
			prDetails.add(prDetailBuilder.build());

		}
		return prDetails;
	}

	private String getPrDetailsData(String pulluri, String accessToken) throws URISyntaxException {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken); // accessToken can be the secret key you generate.
		headers.setContentType(MediaType.APPLICATION_JSON);

		RequestEntity<Void> reqEntity = RequestEntity.get(new URI(pulluri)).headers(headers).build();
		ResponseEntity<String> response = restTemplate.exchange(reqEntity, String.class);

		return response.getBody();
	}

	/**
	 * 
	 * @param prNumber
	 * @param remoteRequest
	 * @return
	 * @throws URISyntaxException
	 */
	private List<PRComment> getPRComments(String uri, String accessToken) throws URISyntaxException {

		List<PRComment> prCommentList = new ArrayList<>();
		System.out.println(uri);
		log.info(uri);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken); // accessToken can be the secret key you generate.
		headers.setContentType(MediaType.APPLICATION_JSON);

		RequestEntity<Void> reqEntity = RequestEntity.get(new URI(uri)).headers(headers).build();
		ResponseEntity<String> response = restTemplate.exchange(reqEntity, String.class);

		String result = response.getBody();
		JSONArray jsonArray = new JSONArray(result);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) (jsonArray.getJSONObject(i));
			prCommentList.add(PRComment.builder().prComment(obj.getString("body"))
					.userName(obj.getJSONObject("user").getString("login")).creationDate(obj.getString("updated_at"))
					.isRvwComment(true).build());
		}
		return prCommentList;
	}

	/**
	 * 
	 * @param uri
	 * @param accessToken
	 * @param title
	 * @return
	 * @throws URISyntaxException
	 */
	private List<PRComment> getPRTimelineComments(String uri, String accessToken, String title)
			throws URISyntaxException {

		List<PRComment> prCommentList = new ArrayList<>();
		System.out.println(uri);
		log.info(uri);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken); // accessToken can be the secret key you generate.
		headers.setContentType(MediaType.APPLICATION_JSON);

		RequestEntity<Void> reqEntity = RequestEntity.get(new URI(uri)).headers(headers).build();
		ResponseEntity<String> response = restTemplate.exchange(reqEntity, String.class);

		String result = response.getBody();
		JSONArray jsonArray = new JSONArray(result);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) (jsonArray.getJSONObject(i));
			if (obj.has("sha") && StringUtils.isNotEmpty(obj.getString("sha"))) {
				String message = obj.getString("message");
				if (!StringUtils.equals(message, title)) {
					JSONObject authorObject = obj.getJSONObject("author");
					prCommentList.add(PRComment.builder().prComment(message).userName(authorObject.getString("email"))
							.creationDate(authorObject.getString("date")).build());
				} else {
					initialComment = message;
				}
			}
			// "event": "review_requested","event": "closed",
			if (StringUtils.isEmpty(createdAt) && obj.has("event")
					&& "review_requested".equalsIgnoreCase(obj.getString("event"))) {
				createdAt = obj.getString("created_at");
			}
			if (obj.has("event") && "closed".equalsIgnoreCase(obj.getString("event"))) {
				closedDate = ZonedDateTime.parse(obj.getString("created_at"), formatter.withZone(ZoneId.of("UTC")));
			}
			if (null != closedDate && StringUtils.isEmpty(prAgeing) && StringUtils.isNotEmpty(createdAt)) {
				Period between = Period.between(
						ZonedDateTime.parse(createdAt, formatter.withZone(ZoneId.of("UTC"))).toLocalDate(),
						closedDate.toLocalDate());
				prAgeing = String.valueOf(between.getDays()) + " day(s)";
			}
		}
		return prCommentList;
	}

}

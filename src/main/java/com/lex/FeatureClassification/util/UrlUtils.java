package com.lex.FeatureClassification.util;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.lex.FeatureClassification.model.RemoteRequest;
import com.lex.FeatureClassification.model.UrlDTO;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

public class UrlUtils {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 
	 * @param remoteRequest
	 * @deprecated
	 */
	public static String prDetailsUrlGenerator(RemoteRequest remoteRequest) {
		String url = remoteRequest.getUrl();
		String repo = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
		StringBuilder sb = new StringBuilder(url);
		sb.delete(sb.lastIndexOf("/"), sb.length());
		String userName = sb.substring(sb.lastIndexOf("/") + 1, sb.length());
		sb.delete(sb.lastIndexOf("/"), sb.length());
		url = sb.toString();
		String uri = url + "/api/v3/repos/" + userName + "/" + repo + "/pulls?state=all&per_page=100";
		return uri;
	}

	public static String getPrUrl(RemoteRequest remoteRequest) {
		// https://github.anaplan.com/api/v3/search/issues?per_page=1&page=1&q=repo:sdp/pathfinder+is:pr+created:2023-09-22..2023-10-06&sort=created&order=asc
		UrlDTO urlDTO = formatNsetUrl(remoteRequest.getUrl());
		String prUrl = urlDTO.getHostUrl() + "/api/v3/search/issues?per_page=100&page=1&q=repo:"
				+ urlDTO.getOwnerRepoPart().substring(1) + "+is:pr+created:" + dateFormat.format(remoteRequest.getDateFrom()) + ".."
				+ dateFormat.format(remoteRequest.getDateTo());
		return prUrl;
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	private static UrlDTO formatNsetUrl(String url) {
		UrlDetector parser = new UrlDetector(url, UrlDetectorOptions.Default);
		Url parsedUrl = parser.detect().get(0);
		String hostUrl = parsedUrl.getScheme()+"://"+parsedUrl.getHost();
		String ownerRepoPart = parsedUrl.getPath().split(".git")[0];
		String apiUrl = hostUrl + "/api/v3/repos" + ownerRepoPart + "/";
		return UrlDTO.builder().hostUrl(hostUrl).ownerRepoPart(ownerRepoPart).apiUrl(apiUrl).build();
	}
	
	
	public static long calculateDifferenceInDays(ZonedDateTime from, ZonedDateTime now) {
        long secondsDiff = now.toEpochSecond() - from.toEpochSecond();
        return TimeUnit.DAYS.convert(secondsDiff, TimeUnit.SECONDS);
    }
	
//	public static void main(String[] args) throws RestClientException, URISyntaxException {
//		System.out.println(ZonedDateTime.now());
//		UrlDTO urlDTO = formatNsetUrl("https://github.anaplan.com/sdp/pathfinder.git");
//		String datefr = "2023-09-20";
//		String dateto = "2023-09-30";
//		String prUrl = urlDTO.getHostUrl() + "/api/v3/search/issues?per_page=100&page=1&q=repo:"
//				+ urlDTO.getOwnerRepoPart().substring(1) + "+is:pr+created:" + datefr + ".."
//				+ dateto + "&sort=created&order=asc";
//		
//		System.out.println(prUrl);
//		
//		RestTemplate restTemplate = new RestTemplate();
//		
//		HttpHeaders headers = new HttpHeaders();
//		headers.setBearerAuth("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");
//		RequestEntity<Void> reqEntity = RequestEntity.get(new URI(prUrl)).headers(headers).build();
//		ResponseEntity<String> exchange = restTemplate.exchange(reqEntity, String.class);
//		System.out.println(exchange);
//	}

}

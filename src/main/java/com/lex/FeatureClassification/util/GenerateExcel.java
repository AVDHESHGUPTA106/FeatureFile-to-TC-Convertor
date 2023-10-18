package com.lex.FeatureClassification.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lex.FeatureClassification.model.PRComment;
import com.lex.FeatureClassification.model.PRDetail;

public class GenerateExcel {

	private static String[] columns = { "Raised By", "Reviewed By", "Repo Name", "PR Number", "PR Link", "PR Title", "From Branch",
			"To Branch", "PR Status", "Creation Date", "PR Ageing", "PR Description", "PR Comment" };
	
	public static ByteArrayInputStream generateExcel(List<PRDetail> prDetailsList) {

		try {

			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("PR Details");
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.BLACK.getIndex());

			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerCellStyle.setAlignment(HorizontalAlignment.LEFT);
			setRowBorderStyle(headerCellStyle);
			headerCellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
			headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle rowCellStyle = workbook.createCellStyle();
			setRowAlignment(rowCellStyle);
			setRowBorderStyle(rowCellStyle);

			Font childFont = workbook.createFont();
			childFont.setFontHeightInPoints((short) 10);
			childFont.setColor(IndexedColors.BLACK.getIndex());
			childFont.setItalic(true);

			CellStyle childUsrRowCellStyle = workbook.createCellStyle();
			childUsrRowCellStyle.setFont(childFont);
			setRowAlignment(childUsrRowCellStyle);
			//childUsrRowCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			//childUsrRowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setRowBorderStyle(childUsrRowCellStyle);

			CellStyle childRowCellStyle = workbook.createCellStyle();
			childRowCellStyle.setFont(childFont);
			setRowAlignment(childRowCellStyle);
			//childRowCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
			//childRowCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setRowBorderStyle(childRowCellStyle);
			
	        CellStyle linkStyle = workbook.createCellStyle(); 
	        Font linkFont = workbook.createFont(); 
	        linkFont.setUnderline(XSSFFont.U_SINGLE); 
	        linkFont.setColor(IndexedColors.BLUE.index); 
	        linkFont.setFontHeightInPoints((short) 10);
	        linkFont.setItalic(true);
	        linkStyle.setFont(linkFont); 
	        setRowAlignment(linkStyle);
	        setRowBorderStyle(linkStyle);
	  

			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			int rowNum = 1;

			// To write PR Details into the excel sheet

			for (PRDetail prDetail : prDetailsList) {

				prepareExcelCell(sheet, rowCellStyle, prDetail, rowNum++, prDetail.getPrUserName(),
						prDetail.getPrCreationDate(), prDetail.getPrComments(), false, linkStyle);

				// Fill Child Sheet
				List<PRComment> prCommentList = prDetail.getPrCommentList();
				if (!prCommentList.isEmpty()) {
					for (PRComment prComment : prCommentList) {
						prDetail.setParent(false);
						prepareExcelCell(sheet, prComment.isRvwComment() ? childRowCellStyle : childUsrRowCellStyle,
								prDetail, rowNum++, prComment.getUserName(), prComment.getCreationDate(),
								prComment.getPrComment(), prComment.isRvwComment(), linkStyle);
					}

				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			workbook.close();
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param rowCellStyle
	 */
	private static void setRowAlignment(CellStyle rowCellStyle) {
		rowCellStyle.setAlignment(HorizontalAlignment.LEFT);
		rowCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
		rowCellStyle.setWrapText(true);
		rowCellStyle.setAlignment(HorizontalAlignment.LEFT);
	}

	/**
	 * 
	 * @param rowCellStyle
	 */
	private static void setRowBorderStyle(CellStyle rowCellStyle) {
		rowCellStyle.setBorderTop(BorderStyle.MEDIUM);
		rowCellStyle.setBorderBottom(BorderStyle.MEDIUM);
		rowCellStyle.setBorderLeft(BorderStyle.MEDIUM);
		rowCellStyle.setBorderRight(BorderStyle.MEDIUM);
	}

	/**
	 * 
	 * @param sheet
	 * @param rowCellStyle
	 * @param prDetail
	 * @param rowNum
	 * @param prUsrName
	 * @param rvwComntDate
	 * @param prRvwComnt
	 * @param linkStyle 
	 * @param link 
	 */
	private static void prepareExcelCell(Sheet sheet, CellStyle rowCellStyle, PRDetail prDetail, int rowNum,
			String prUsrName, String rvwComntDate, String prRvwComnt, boolean isrvwComnt, CellStyle linkStyle) {
		Row row = sheet.createRow(rowNum);
		Cell zeroCell = row.createCell(0);
		zeroCell.setCellValue(!isrvwComnt ? prUsrName : StringUtils.EMPTY);
		zeroCell.setCellStyle(rowCellStyle);

		Cell firstCell = row.createCell(1);
		firstCell.setCellStyle(rowCellStyle);
		firstCell.setCellValue(!isrvwComnt ? StringUtils.EMPTY : prUsrName);

		Cell secondCell = row.createCell(2);
		secondCell.setCellValue(prDetail.getRepoName());
		secondCell.setCellStyle(rowCellStyle);
		
		Cell prNumberCell = row.createCell(3);
		prNumberCell.setCellValue(prDetail.getPrNumber());
		prNumberCell.setCellStyle(rowCellStyle);

		Cell thirdCell = row.createCell(4);
		thirdCell.setCellValue(prDetail.getPrLink());
		XSSFHyperlink link = (XSSFHyperlink)sheet.getWorkbook().getCreationHelper().createHyperlink(HyperlinkType.URL);
        link.setAddress(prDetail.getPrLink()); 
        thirdCell.setHyperlink((XSSFHyperlink)link); 
        thirdCell.setCellStyle(linkStyle); 

		Cell fourthCell = row.createCell(5);
		fourthCell.setCellValue(prDetail.getPrTitle());
		fourthCell.setCellStyle(rowCellStyle);

		Cell fifthCell = row.createCell(6);
		fifthCell.setCellValue(prDetail.getPrFrmBranchName());
		fifthCell.setCellStyle(rowCellStyle);

		Cell sixthCell = row.createCell(7);
		sixthCell.setCellValue(prDetail.getPrToBranchName());
		sixthCell.setCellStyle(rowCellStyle);

		Cell seventhCell = row.createCell(8);
		seventhCell.setCellValue(prDetail.getPrStatus());
		seventhCell.setCellStyle(rowCellStyle);

		Cell eightCell = row.createCell(9);
		eightCell.setCellValue(rvwComntDate);
		eightCell.setCellStyle(rowCellStyle);

		Cell ninthCell = row.createCell(10);
		ninthCell.setCellValue(prDetail.isParent() ? prDetail.getPrAgeing() : StringUtils.EMPTY);
		ninthCell.setCellStyle(rowCellStyle);

		Cell tenthCell = row.createCell(11);
		tenthCell.setCellValue(prDetail.isParent() ? prDetail.getPrDescription() : StringUtils.EMPTY);
		tenthCell.setCellStyle(rowCellStyle);

		Cell cell = row.createCell(12);
		cell.setCellValue(prRvwComnt != "null" ? prRvwComnt : StringUtils.EMPTY);
		cell.setCellStyle(rowCellStyle);// Wrapping text

		sheet.setDefaultColumnWidth(20);
	}
}

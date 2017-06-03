package com.abbyy.ocrsdk;

public class ProcessingSettings {

	private String language = "English";
	private OutputFormat outputFormat = OutputFormat.pdfSearchable;
	private OCRProfile profile;
	private String recTextType;
	private ImageSource ImageSource;
	private String isCorrectOrientation;
	private String isCorrectSkew;
	private String isReadBarcodes;
	private String isWriteRecognitionVariants;

public String asUrlParams() {
	return String.format(
			inputLanguauge() +
			exportFormat() +
			profileType() +
			textType() +
			imageSource() +
			correctOrientation() +
			correctSkew() +
			readBarcodes()
	);
}

	public String inputLanguauge(){
		return String.format("language=%s", language);
	}

	public String exportFormat(){
		return String.format("&exportFormat=%s", outputFormat);
	}

	public String profileType(){
		if (profile != null) {
			return String.format("&profile=%s", profile);
		}
		else return "";
	}

	public String textType(){
		if (recTextType != null) {
			return String.format("&textType=%s", recTextType);
		}
		else return "";
	}

	public String imageSource(){
		if (ImageSource != null) {
			return String.format("&imageSource=%s", ImageSource);
		}
		else return "";
	}

	public String correctOrientation(){
		if (isCorrectOrientation != null) {
			return String.format("&correctOrientation=%s", isCorrectOrientation);
		}
		else return "";
	}

	public String correctSkew(){
		if (isCorrectSkew != null) {
			return String.format("&correctSkew=%s", isCorrectSkew);
		}
		else return "";
	}

	public String readBarcodes(){
		if (isReadBarcodes != null) {
			return String.format("&readBarcodes=%s", isReadBarcodes);
		}
		else return "";
	}

	public String writeRecognitionVariants(){
		if (isWriteRecognitionVariants != null) {
			return String.format("&xml:writeRecognitionVariants=%s", isWriteRecognitionVariants);
		}
		else return "";
	}

	public enum OutputFormat {
		txt, rtf, docx, xlsx, pptx, pdfSearchable, pdfTextAndImages, xml
	}

	public void setOutputFormat(OutputFormat format) {
		this.outputFormat = format;
	}

	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	
	public String getOutputFileExt() {
		switch( outputFormat ) {
			case txt: return ".txt";
			case rtf: return ".rtf";
			case docx: return ".docx";
			case xlsx: return ".xlsx";
			case pptx: return ".pptx";
			case pdfSearchable:
			case pdfTextAndImages: return ".pdf";
			case xml: return ".xml";
		}
		return ".ocr";
	}

	/*
	 * Set recognition language. You can set any language listed at
	 * http://ocrsdk.com/documentation/specifications/recognition-languages/ or
	 * set comma-separated combination of them.
	 * 
	 * Examples: English English,ChinesePRC English,French,German
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public enum OCRProfile {
		documentConversion, documentArchiving, textExtraction, fieldLevelRecognition, barcodeRecognition
	}

	public String getOCRProfile(){
		switch (profile){
			case documentConversion: return "documentConversion";
			case documentArchiving: return "documentArchiving";
			case textExtraction: return "textExtraction";
			case fieldLevelRecognition: return "fieldLevelRecognition";
			case barcodeRecognition: return "barcodeRecognition";
		}
		return "textExtraction";
	}

	public void setProfile(OCRProfile Profile){
		this.profile = Profile;
	}

	public void setTextType(String textType) {
		this.recTextType = textType;
	}

	public enum ImageSource {
		auto, photo, scanner
	}

	public String getImageSource(){
		switch (ImageSource){
			case auto: return "auto";
			case photo: return "photo";
			case scanner: return "scanner";
		}
		return "auto";
	}

	public void setImageSource(ImageSource imageSource) {
		this.ImageSource = imageSource;
	}

	public void setCorrectOrientation(String isCorrectOrientation) {
		this.isCorrectOrientation = isCorrectOrientation;
	}

	public void setIsCorrectSkew(String getIsCorrectSkew) {
		this.isCorrectSkew = getIsCorrectSkew;
	}

	public void setIsReadBarcodes(String isReadBarcodes) {
		this.isReadBarcodes = isReadBarcodes;
	}

	public void setIsWriteRecognitionVariants(String isWriteRecognitionVariants) {
		this.isWriteRecognitionVariants = isWriteRecognitionVariants;
	}
}

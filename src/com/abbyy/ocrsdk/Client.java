package com.abbyy.ocrsdk;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Client {
	public String applicationId;
	public String password;

	public String serverUrl = "http://cloud.ocrsdk.com";
	private static String pathForStatus = "C:\\Users\\DRSPEED-PC\\Documents\\TEST_ABBYY\\Status.xml";
	private static final int BUFFER_SIZE = 4096;

	/*
	 * Upload image to server and optionally append it to existing task. If
	 * taskId is null, creates new task.
	 */
	public Task submitImage(String filePath, String taskId) throws Exception {
		String taskPart = "";
		if (taskId != null && !taskId.isEmpty()) {
			taskPart = "?taskId=" + taskId;
		}
		URL url = new URL(serverUrl + "/submitImage" + taskPart);
		return postFileToUrl(filePath, url);
	}

	public Task processImage(String filePath, ProcessingSettings settings)
			throws Exception {
		URL url = new URL(serverUrl + "/processImage?" + settings.asUrlParams());
		return postFileToUrl(filePath, url);
	}

	public Task processRemoteImage( String fileUrl, ProcessingSettings settings)
			throws Exception {
		URL url = new URL(String.format("%s/processRemoteImage?source=%s&%s",
			serverUrl, URLEncoder.encode(fileUrl, "UTF-8"), settings.asUrlParams()));

		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}

	public Task processDocument(String taskId, ProcessingSettings settings)
			throws Exception {
		URL url = new URL(serverUrl + "/processDocument?taskId=" + taskId + "&"
				+ settings.asUrlParams());

		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}

	public Task processBusinessCard(String filePath, BusCardSettings settings)
			throws Exception {
		URL url = new URL(serverUrl + "/processBusinessCard?"
				+ settings.asUrlParams());
		return postFileToUrl(filePath, url);
	}

	public Task processTextField(String filePath, TextFieldSettings settings)
			throws Exception {
		URL url = new URL(serverUrl + "/processTextField?"
				+ settings.asUrlParams());
		return postFileToUrl(filePath, url);
	}

	public Task processBarcodeField(String filePath, BarcodeSettings settings)
			throws Exception {
		URL url = new URL(serverUrl + "/processBarcodeField?"
				+ settings.asUrlParams());
		return postFileToUrl(filePath, url);
	}

	public Task processCheckmarkField(String filePath) throws Exception {
		URL url = new URL(serverUrl + "/processCheckmarkField");
		return postFileToUrl(filePath, url);
	}

	/**
	 * Recognize multiple text, barcode and checkmark fields at one call.
	 * 
	 * For details see
	 * http://ocrsdk.com/documentation/apireference/processFields/
	 * 
	 * @param settingsPath
	 *            path to xml file describing processing settings
	 */
	public Task processFields(String taskId, String settingsPath)
			throws Exception {
		URL url = new URL(serverUrl + "/processFields?taskId=" + taskId);
		return postFileToUrl(settingsPath, url);
	}
	
	
	/**
	 * Process and parse Machine-Readable Zone (MRZ) of Passport, ID card, Visa etc
	 * 
	 * For details see
	 * http://ocrsdk.com/documentation/apireference/processMRZ/
	 * 
	 */
	public Task processMrz(String filePath ) throws Exception {
		URL url = new URL(serverUrl + "/processMrz" );
		return postFileToUrl(filePath, url);
	}
	
	public Task getTaskStatus(String taskId) throws Exception {
		URL url = new URL(serverUrl + "/getTaskStatus?taskId=" + taskId);

		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}

	public Task[] listFinishedTasks() throws Exception {
		URL url = new URL(serverUrl + "/listFinishedTasks");
		HttpURLConnection connection = openGetConnection(url);
		return getTaskListResponse(connection);
	}

	private SortedMap<String, WordDto> processResult(BufferedInputStream stream) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(stream);
		doc.getDocumentElement().normalize();
		SortedMap<String, SortedMap<Pair<Integer, Integer>, SymbolNode>> resultUnhandled = new TreeMap<>();
		NodeList textList = doc.getElementsByTagName("text");
		final BiFunction<Pair<Integer, Integer>, Pair<Integer, Integer>, Integer> pairComparator = (pair1, pair2) -> {
			int res = pair1.getKey().compareTo(pair2.getKey());
			if (res == 0) {
				return pair1.getValue().compareTo(pair2.getValue());
			}
			return res;
		};
		for (int textInd = 0; textInd < textList.getLength(); textInd++) {
			SortedMap<Pair<Integer, Integer>, SymbolNode> curLine = new TreeMap<>(pairComparator::apply);
			Node textNode = textList.item(textInd);
			if (textNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NodeList charList = ((Element) textNode).getElementsByTagName("char");
			for (int charInd = 0; charInd < charList.getLength(); charInd++) {
				Node charNode = charList.item(charInd);
				if (charNode.getNodeType() != Node.ELEMENT_NODE || "^".equals(charNode.getTextContent())) {
					continue;
				}
				int left = Integer.parseInt(charNode.getAttributes().getNamedItem("left").getNodeValue());
				int top = Integer.parseInt(charNode.getAttributes().getNamedItem("top").getNodeValue());
				Integer leftI = left - (left % 5);
				Integer topI = top - (top % 5);
				Node suspNode = charNode.getAttributes().getNamedItem("suspicious");
				boolean susp = suspNode != null && Boolean.parseBoolean(suspNode.getNodeValue());
				SymbolNode symbol = new SymbolNode(charNode.getTextContent(), susp ? 1 : 2, 0);
				curLine.put(new Pair<>(leftI, topI), symbol);
			}
			resultUnhandled.put(textNode.getAttributes().getNamedItem("id").getNodeValue(), curLine);
		}

		SortedMap<String, SortedMap<Pair<Integer, Integer>, SortedMap<String, Integer>>> resultProcessed = new TreeMap<>();
		resultUnhandled.forEach((field, map) -> {
			String curKey = field.substring(0, field.length() - 1);
			if (!resultProcessed.containsKey(curKey)) {
				SortedMap<Pair<Integer, Integer>, SortedMap<String, Integer>> resMap = new TreeMap<>(pairComparator::apply);
				resultProcessed.put(curKey, resMap);
			}
			map.forEach((pair, symbol) -> {
				if (!resultProcessed.get(curKey).containsKey(pair)) {
					resultProcessed.get(curKey).put(pair, new TreeMap<>());
				}
				int byChar = resultProcessed.get(curKey).get(pair).getOrDefault(symbol.getValue(), 0);
				resultProcessed.get(curKey).get(pair).put(symbol.getValue(), byChar + symbol.getWeight());
				int total = resultProcessed.get(curKey).get(pair).getOrDefault("total", 0);
				resultProcessed.get(curKey).get(pair).put("total", total + symbol.getWeight());
			});
		});
		SortedMap<String, WordDto> result = new TreeMap<>();
		resultProcessed.forEach((field, map) -> {
			WordDto resWord = map.entrySet().stream().reduce(new WordDto("", false), (word, elem) -> {
				Optional<Map.Entry<String, Integer>> max = elem.getValue().entrySet().stream()
						.filter(l -> !"total".equals(l.getKey()))
						.max(Comparator.comparingInt(Map.Entry::getValue));
				if (elem.getValue().get("total") >= 3) {
					max.ifPresent(stringIntegerEntry -> word.setValue(word.getValue() + stringIntegerEntry.getKey()));
					if (elem.getValue().get("total") < 5) {
						word.setSuspicious(true);
					}
				}
				return word;
			}, (el1, el2) -> new WordDto(el1.getValue() + el2.getValue(), el1.getSuspicious() || el2.getSuspicious()));
			result.put(field, resWord);
		});
		return result;
//		resultProcessed.forEach((field, map) -> {
//			System.out.println("\n" + field);
//			String result = map.values().stream()
//					.map(SymbolNode::getValue)
//					.reduce("", (res, val) -> res + val);
//			System.out.println(" : " + result);
//		});
	}

	public SortedMap<String, WordDto> downloadResult(Task task, String outputFile) throws Exception {
		if (task.Status != Task.TaskStatus.Completed) {
			throw new IllegalArgumentException("Invalid task status");
		}

		if (task.DownloadUrl == null) {
			throw new IllegalArgumentException(
					"Cannot download result without url");
		}

		URL url = new URL(task.DownloadUrl);
		URLConnection connection = url.openConnection(); // do not use
															// authenticated
															// connection

		BufferedInputStream reader = new BufferedInputStream(
				connection.getInputStream());
		SortedMap<String, WordDto> result = processResult(reader);

		FileOutputStream out = new FileOutputStream(outputFile);

		try {
			byte[] data = new byte[1024];
			int count;
			while ((count = reader.read(data, 0, data.length)) != -1) {
				out.write(data, 0, count);
			}
		} finally {
			out.close();
		}
		return result;
	}

	public Task deleteTask(String taskId) throws Exception {
		URL url = new URL(serverUrl + "/deleteTask?taskId=" + taskId);

		HttpURLConnection connection = openGetConnection(url);
		return getResponse(connection);
	}


	private HttpURLConnection openPostConnection(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		setupAuthorization(connection);
		connection
				.setRequestProperty("Content-Type", "application/octet-stream");

		return connection;
	}

	private HttpURLConnection openGetConnection(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// connection.setRequestMethod("GET");
		setupAuthorization(connection);
		return connection;
	}

	private void setupAuthorization(URLConnection connection) {
		String authString = "Basic " + encodeUserPassword();
		authString = authString.replaceAll("\n", "");
		connection.addRequestProperty("Authorization", authString);
	}

	private byte[] readDataFromFile(String filePath) throws Exception {
		File file = new File(filePath);
		long fileLength = file.length();
		byte[] dataBuffer = new byte[(int) fileLength];

		InputStream inputStream = new FileInputStream(file);
		try {

			int offset = 0;
			int numRead = 0;
			while (true) {
				if (offset >= dataBuffer.length) {
					break;
				}
				numRead = inputStream.read(dataBuffer, offset, dataBuffer.length - offset);
				if (numRead < 0) {
					break;
				}
				offset += numRead;
			}
			if (offset < dataBuffer.length) {
				throw new IOException("Could not completely read file "
						+ file.getName());
			}
		} finally {
			inputStream.close();
		}
		return dataBuffer;
	}

	private Task postFileToUrl(String filePath, URL url) throws Exception {
		byte[] fileContents = readDataFromFile(filePath);

		HttpURLConnection connection = openPostConnection(url);
		connection.setRequestProperty("Content-Length", Integer.toString(fileContents.length));

		OutputStream stream = connection.getOutputStream();
		try {
			stream.write(fileContents);
		} finally {
			stream.close();
		}
		//writeResponseToFile(connection, pathForStatus);
		return getResponse(connection);
	}

	private String encodeUserPassword() {
		String toEncode = applicationId + ":" + password;
		return Base64.encode(toEncode);
	}

	/**
	 * Read server response from HTTP connection and return task description.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	private Task getResponse(HttpURLConnection connection) throws Exception {
		int responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			return new Task(reader);
		} else if (responseCode == 401) {
			throw new Exception(
					"HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			throw new Exception("HTTP 407. Proxy authentication error");
		} else {
			String message = "";
			try {
				InputStream errorStream = connection.getErrorStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(errorStream));

				// Parse xml error response
				InputSource source = new InputSource();
				source.setCharacterStream(reader);
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(source);
				
				NodeList error = doc.getElementsByTagName("error");
				Element err = (Element) error.item(0);
				
				message = err.getTextContent();
			} catch (Exception e) {
				throw new Exception("Error getting server response");
			}

			throw new Exception("Error: " + message);
		}
	}

	private Task[] getTaskListResponse(HttpURLConnection connection) throws Exception {
		int responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			return Task.LoadTasks( reader );
		} else if (responseCode == 401) {
			throw new Exception(
					"HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			throw new Exception("HTTP 407. Proxy authentication error");
		} else {
			String message = "";
			try {
				InputStream errorStream = connection.getErrorStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(errorStream));

				// Parse xml error response
				InputSource source = new InputSource();
				source.setCharacterStream(reader);
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();
				Document doc = builder.parse(source);
				
				NodeList error = doc.getElementsByTagName("error");
				Element err = (Element) error.item(0);
				
				message = err.getTextContent();
			} catch (Exception e) {
				throw new Exception("Error getting server response");
			}

			throw new Exception("Error: " + message);
		}
	}

	public void writeResponseToFile(HttpURLConnection connection, String saveDir) throws Exception{
		InputStream writeInputStream = connection.getInputStream();
		BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(writeInputStream));
		FileOutputStream fileOutputStream = new FileOutputStream(saveDir, false);
		int i;
		while ((i = bufferedInputStream.read()) != -1){
			fileOutputStream.write(i);
		}
		fileOutputStream.close();
		System.out.println("State file has been downloaded");
	}


}

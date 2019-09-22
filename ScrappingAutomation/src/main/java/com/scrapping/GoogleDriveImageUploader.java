package com.scrapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * Work in progress, need to see how to upload file to a specific drive folder
 * 
 * @author manas.saxena
 *
 */
@Component
public class GoogleDriveImageUploader {

	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	private static final String TO_BE_UPLOADED_PREFIX = "TOBEUPLOADED_";
	private static final String COMPLETED_UPLOAD_PREFIX = "COMPLETED_";

	@Value("${com.scrapping.drive.toBePosted.folderId}")
	private String UPLOAD_FOLDER_ID = "1vqStSmKuBVWZ8TgD_VBvg4c_DdbPBkEm";

	public void uploadImage(String fullLocalFilePath, String fileName) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		File fileMetadata = new File();
		fileMetadata.setName(TO_BE_UPLOADED_PREFIX + fileName);
		fileMetadata.setParents(Collections.singletonList(UPLOAD_FOLDER_ID));
		java.io.File filePath = new java.io.File(fullLocalFilePath);
		FileContent mediaContent = new FileContent("image/jpeg", filePath);
		File file = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();
		System.out.println("File ID: " + file.getId());
	}

	public void rename(String fileName) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		String pageToken = null;

		do {
			FileList result = driveService.files().list().setQ("mimeType='image/jpeg'").setSpaces("drive")
					.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
			for (File fileSearch : result.getFiles()) {
				if (fileSearch.getName().equals(fileName)) {
					String fileId = fileSearch.getId();
					String newName = fileSearch.getName().replace(TO_BE_UPLOADED_PREFIX, COMPLETED_UPLOAD_PREFIX);
					File newFile = new File();
					newFile.setName(newName);
					File updatedFile = driveService.files().update(fileId, newFile).execute();
					return;
				}
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null);
	}

	public boolean fileExist(String fileName) {
		NetHttpTransport HTTP_TRANSPORT;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

			Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			String pageToken = null;

			do {
				FileList result = driveService.files().list().setQ("mimeType='image/jpeg'").setSpaces("drive")
						.setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
				for (File fileSearch : result.getFiles()) {
					String coreFileName =  fileSearch.getName().replace(COMPLETED_UPLOAD_PREFIX, "");
					coreFileName =  fileSearch.getName().replace(TO_BE_UPLOADED_PREFIX, "");
					if (fileName.equals(coreFileName)) {
						return true;
					}
				}
				pageToken = result.getNextPageToken();
			} while (pageToken != null);

		} catch (Exception e) {
			throw new RuntimeException("Failed to check file exist in drive");
		}
		return false;
	}

	public java.io.File downloadFirstFile(String fileDirectory) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		FileList result = driveService.files().list().setQ("name contains '" + TO_BE_UPLOADED_PREFIX + "'")
				.setPageSize(1000).setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			System.out.println("No files found.");
			return null;
		} else {

			System.out.println("Files:");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			driveService.files().get(files.get(0).getId()).executeMediaAndDownloadTo(bos);
			InputStream is = new ByteArrayInputStream(bos.toByteArray());
			java.io.File localFile = new java.io.File(fileDirectory + "/" + files.get(0).getName());
			FileUtils.copyInputStreamToFile(is, localFile);
			return localFile;

		}
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GoogleDriveImageUploader.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, DriveScopes.all())
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
}
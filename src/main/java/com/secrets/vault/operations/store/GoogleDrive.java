package com.secrets.vault.operations.store;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;
import com.secrets.vault.operations.common.DriveService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleDrive {

    private static final String folderName = "SecureStorage";

    private static String getParentFolderId() throws GeneralSecurityException, IOException {
        Drive service  = new DriveService().getDriveService();

        String query = "name = '" + folderName + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false";
        FileList result = service.files().list()
                .setQ(query)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            throw new IOException("No Secure folder found.");
        } else {
            if(files.size() == 1) {
                File folder = files.get(0);
                System.out.printf("%s (%s)\n", folder.getName(), folder.getId());
                return folder.getId();
            }else{
                throw new IOException("More than one secure folders");
            }
        }
    }

    public static void listSecrets() throws IOException, GeneralSecurityException {
        // Print the names and IDs for up to 10 files.
        Drive service  = new DriveService().getDriveService();
        String query = "'" + GoogleDrive.getParentFolderId() + "' in parents and trashed = false";
        FileList result = service.files().list()
                .setQ(query)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        System.out.println("Files:");
        for (File file : files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId());
        }
    }

    public static String UploadSecrets(String filePath) throws GeneralSecurityException, IOException {
        Drive service  = new DriveService().getDriveService();
        java.io.File uploadFile = new java.io.File(filePath);
        if (!uploadFile.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        File fileMetadata = new File();
        fileMetadata.setName(uploadFile.getName());
        String parentFolderId = GoogleDrive.getParentFolderId();
        if (parentFolderId != null) {
            fileMetadata.setParents(Collections.singletonList(parentFolderId));
        }

        FileContent mediaContent = new FileContent(
                // Auto-detect the MIME type or set it manually
                "application/octet-stream",
                uploadFile
        );

        // Files.Create supports resumable uploads by default for large files.
        Drive.Files.Create createRequest = service.files().create(fileMetadata, mediaContent);

        // Disable direct upload to ensure resumable protocol is used
        createRequest.getMediaHttpUploader().setDirectUploadEnabled(false);

        // Optional: Set a chunk size (must be a multiple of 256KB)
        // createRequest.getMediaHttpUploader().setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 4);

        // Add a progress listener to monitor the upload state
        createRequest.getMediaHttpUploader().setProgressListener(uploader -> {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    System.out.println("Initiation has started!");
                    break;
                case INITIATION_COMPLETE:
                    System.out.println("Initiation is complete!");
                    break;
                case MEDIA_IN_PROGRESS:
                    System.out.printf("Upload progress: %.2f%%\n", uploader.getProgress() * 100);
                    break;
                case MEDIA_COMPLETE:
                    System.out.println("Upload is complete!");
                    break;
            }
        });
        System.out.println("Starting resumable upload for file: " + uploadFile.getName());
        File uploadedFile = createRequest.execute();
        System.out.printf("File uploaded successfully. Name: %s, ID: %s\n", uploadedFile.getName(), uploadedFile.getId());
        // To do upload files
        return uploadedFile.getId();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        //String uploadId = DriveOperations.UploadSecrets("D:\\Learnings\\Java\\SecretManagerWorkspace\\SecretManger\\src\\main\\resources\\uploadtest.txt");
        //System.out.println("Uploaded "+uploadId);
        listSecrets();
    }
}
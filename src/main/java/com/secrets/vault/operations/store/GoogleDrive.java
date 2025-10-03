package com.secrets.vault.operations.store;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.FileContent;
import com.secrets.vault.operations.common.DriveService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    public List<File> listSecrets() throws IOException, GeneralSecurityException {
        // Print the names and IDs for up to 10 files.
        Drive service  = new DriveService().getDriveService();
        String query = "'" + GoogleDrive.getParentFolderId() + "' in parents and trashed = false";
        FileList result = service.files().list()
                .setQ(query)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        System.out.println("================================");
        System.out.println("Files:");
        System.out.println("--------------------------------");
        for (File file : files) {
            System.out.println("File Id : "+file.getId());
            System.out.println("File Name : "+file.getName());
        }
        System.out.println("================================");
        return files;
    }

    public String UploadSecrets(String filePath) throws GeneralSecurityException, IOException {
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

    public void updateFile(String fileId, String fileToUpload) throws IOException, GeneralSecurityException {
        Drive driveService = new DriveService().getDriveService();
        // Create a File object for the updated metadata (optional, can be null if only content is updated)
        File fileMetadata = new File();
        // You can update metadata fields like name, description, etc. if needed
        // fileMetadata.setName("New File Name");

        // Create a FileContent object with the new content
        FileContent mediaContent = new FileContent(null, new java.io.File(fileToUpload)); // Mime type will be detected automatically

        try {
            // Perform the update operation
            File updatedFile = driveService.files().update(fileId,fileMetadata,mediaContent).execute();
            System.out.println("File updated successfully. New file ID: " + updatedFile.getId());
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Downloads a file from Google Drive.
     * @param service The Drive service instance.
     * @param fileId The ID of the file to download.
     * @param downloadPath The local path to save the file.
     * @throws IOException
     */
    public void downloadFile(String fileId, String downloadPath) throws IOException, GeneralSecurityException {
        Drive service = new DriveService().getDriveService();
        try (OutputStream outputStream = new FileOutputStream(downloadPath)) {
            System.out.println("Downloading standard file content for file ID: " + fileId);

            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            System.out.println("Download successful! File saved to: " + downloadPath);
        } catch (IOException e) {
            System.err.println("An error occurred during file download: " + e.getMessage());
            throw e; // Re-throw the exception for further handling
        }
    }
}
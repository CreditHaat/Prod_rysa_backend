package com.lsp.web.ONDCService;

import java.io.File;

import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;


@Service
public class S3UploaderService {

	//here removing this just to push on git
    private final String accessKey = "";
    private final String secretKey = "";
    private final String bucketName = "ch-analysis-bucket";
    private final String region = "ap-south-1";
    // private final String folderPrefix = "MIS/"; //========>preprod or testing
    
    private final String folderPrefix = "LIVE_MIS_PROD/";//====>live s3 bucket


    public String uploadFileToS3(File file) {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(Regions.fromName(region))
                    .build();

            String fileKey = folderPrefix + file.getName();
            s3Client.putObject(bucketName, fileKey, file);

            // Get S3 file URL
            String fileUrl = s3Client.getUrl(bucketName, fileKey).toString();
            System.out.println("Uploaded file to S3: " + fileUrl);

            return fileUrl; // Return URL to be sent via email
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error uploading to S3: " + e.getMessage());
            return null;
        }
    }

}



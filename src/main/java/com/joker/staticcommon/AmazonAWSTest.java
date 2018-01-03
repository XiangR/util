package com.joker.staticcommon;

import java.io.File;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * 
 * @author xiangR
 * @date 2017年7月28日上午9:47:16
 *
 */
public class AmazonAWSTest {

	static String accessKeyID = "";
	static String secretKey = "";
	static String myBucketName = "";
	static AmazonS3 s3Client;

	public static void main(String[] args) {

		ClientConfiguration configuration = new ClientConfiguration();
		configuration.setConnectionTimeout(10 * 1000);
		configuration.setSocketTimeout(10 * 1000);
		configuration.setProtocol(Protocol.HTTP);

		AWSCredentials credentials = new BasicAWSCredentials(accessKeyID, secretKey);
		s3Client = new AmazonS3Client(credentials, configuration);
		// watchBucket(s3Client);
		watchObject2(s3Client);
		// uploadFile();
		// deleteFile();
		// uploadFileThread();
	}

	/**
	 * 看 bucket
	 * 
	 * @param s3Client
	 */
	public static void watchBucket(AmazonS3 s3Client) {

		List<Bucket> buckets = s3Client.listBuckets();
		System.out.println(buckets.size());
		for (Bucket bucket : buckets) {
			System.out.println("Bucket: " + bucket.getName());
		}

	}

	/**
	 * 看指定 bucket 下的Object
	 * 
	 * @param s3Client
	 */
	public static void watchObject(AmazonS3 s3Client) {
		ObjectListing objects = s3Client.listObjects(myBucketName);

		do {
			for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
				System.out.println("==============Object: " + objectSummary.getKey());
			}
			objects = s3Client.listNextBatchOfObjects(objects);
		} while (objects.isTruncated());

	}

	public static void watchObject2(AmazonS3 s3Client) {
		String prefix = "photos/raiders/img";
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(myBucketName).withPrefix(prefix);

		ObjectListing objects = s3Client.listObjects(listObjectsRequest);
		for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
			System.out.println("Object: " + objectSummary.getKey());
		}
	}

	public static void watchObject3(AmazonS3 s3Client) {
		String prefix = "";
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(myBucketName).withPrefix(prefix).withDelimiter("/");

		ObjectListing objects = s3Client.listObjects(listObjectsRequest);
		do {
			for (String objKey : objects.getCommonPrefixes()) {
				System.out.println("+ " + objKey);
			}

			for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
				String objKey = objectSummary.getKey();
				System.out.println(objKey);
			}
			objects = s3Client.listNextBatchOfObjects(objects);
		} while (objects.isTruncated());

	}

	public static void deleteFile() {
		String fileName = "1499769520971945.jpg";
		String key = "/photos/raiders/img/" + fileName;
		DeleteObjectRequest request = new DeleteObjectRequest(myBucketName, key);
		s3Client.deleteObject(request);
		System.out.println("删除对象结束");
	}

	public static void deleteFile(String key) {
		DeleteObjectRequest request = new DeleteObjectRequest(myBucketName, key);
		s3Client.deleteObject(request);
		System.out.println("删除对象结束");
	}

	public static void uploadFile() {
		String fileName = "1499769520971943.jpg";
		String localPath = "f:" + fileName;
		String key = "photos/user_base/img/" + fileName;

		File file = new File(localPath);

		// 默认添加public权限
		s3Client.putObject(new PutObjectRequest(myBucketName, key, file).withCannedAcl(CannedAccessControlList.PublicRead));

		// s3Client.putObject(new PutObjectRequest(myBucketName, key, file));
		System.out.println("创建对象结束");
		watchObject(s3Client);
	}

	public static void uploadFileThread() {
		TransferManager tm = new TransferManager(s3Client);
		TransferManagerConfiguration conf = tm.getConfiguration();

		long threshold = 4 * 1024 * 1024;
		conf.setMultipartUploadThreshold(threshold);
		tm.setConfiguration(conf);

		String fileName = "1499769520971942.jpg";
		String localPath = "f:" + fileName;
		String key = "photos/raiders/img/" + fileName;

		Upload upload = tm.upload(myBucketName, key, new File(localPath));
		TransferProgress p = upload.getProgress();
		try {
			while (upload.isDone() == false) {
				int percent = (int) (p.getPercentTransferred());
				System.out.print("\r" + localPath + " - " + "[ " + percent + "% ] " + p.getBytesTransferred() + " / " + p.getTotalBytesToTransfer());
				// Do work while we wait for our upload to complete...

				Thread.sleep(500);

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.print("\r" + localPath + " - " + "[ 100% ] " + p.getBytesTransferred() + " / " + p.getTotalBytesToTransfer());

		// 默认添加public权限
		s3Client.setObjectAcl(myBucketName, key, CannedAccessControlList.PublicRead);
		System.out.print("创建对象成功");
	}

}

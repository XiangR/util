package com.joker.staticcommon;

import java.io.File;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
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
 * @date 2017年7月28日上午9:47:22
 *
 */
public class AmazonAWSUtility {

	private static String accessKeyID = "";
	private static String secretKey = "";
	private static String bucketName = "";

	private static AWSCredentials credentials = new BasicAWSCredentials(accessKeyID, secretKey);

	public static void main(String[] args) {

		// String fileName = "1499851559243262.jpg";
		// String localPath = "f:" + fileName;
		// String key = "photos/raiders/img/" + fileName;

		// watchBucket();
		// watchObject2("photos/raiders");
		watchObject();
		// uploadFile(key, localPath);
		// uploadFile();
		// deleteFile();
		// uploadFileThread();
	}

	/**
	 * 获取client
	 *
	 * @return
	 */
	private static AmazonS3Client getAmazonS3Client() {
		ClientConfiguration clientConfig = new ClientConfiguration();
		// 解决超时异常情况
		clientConfig.setProtocol(Protocol.HTTP);
		clientConfig.setConnectionTimeout(10 * 1000);
		clientConfig.setSocketTimeout(10 * 1000);

		AmazonS3Client amazonS3Client = new AmazonS3Client(credentials, clientConfig);
		return amazonS3Client;
	}

	/**
	 * 看 bucket
	 * 
	 * @param s3Client
	 */
	public static void watchBucket() {
		AmazonS3Client client = getAmazonS3Client();
		List<Bucket> buckets = client.listBuckets();
		System.out.println(buckets.size());
		for (Bucket bucket : buckets) {
			System.out.println("Bucket: " + bucket.getName());
		}
	}

	/*
	 * 看bucket 下的所有object
	 */
	public static void watchObject() {
		AmazonS3Client client = getAmazonS3Client();
		ObjectListing objects = client.listObjects(bucketName);

		do {
			for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
				System.out.println("==============Object: " + objectSummary.getKey());
			}
			objects = client.listNextBatchOfObjects(objects);
		} while (objects.isTruncated());
	}

	/**
	 * 在bucket 下的目录中寻找指定前缀下的所有文件
	 * 
	 * @param prefix
	 *            前缀
	 */
	public static void watchObject2(String prefix) {
		AmazonS3Client client = getAmazonS3Client();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix);

		ObjectListing objects = client.listObjects(listObjectsRequest);
		for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
			System.out.println("Object: " + objectSummary.getKey());
		}
	}

	/**
	 * 在bucket 下的目录中寻找指定前缀下的所有文件
	 * 
	 * @param prefix
	 *            前缀
	 */
	public static void watchObject3(String prefix) {
		AmazonS3Client client = getAmazonS3Client();

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix).withDelimiter("/");

		ObjectListing objects = client.listObjects(listObjectsRequest);
		do {
			for (String objKey : objects.getCommonPrefixes()) {
				System.out.println("+ " + objKey);
			}

			for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
				String objKey = objectSummary.getKey();
				System.out.println(objKey);
			}
			objects = client.listNextBatchOfObjects(objects);
		} while (objects.isTruncated());

	}

	public static void deleteFile() {
		AmazonS3Client client = getAmazonS3Client();
		String fileName = "1499769520971945.jpg";
		String key = "/photos/raiders/img/" + fileName;
		DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);
		client.deleteObject(request);
		System.out.println("删除对象结束");
	}

	/**
	 * 删除指定的Object
	 * 
	 * @param key
	 *            位置
	 */
	public static void deleteFile(String key) {
		AmazonS3Client client = getAmazonS3Client();
		DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);
		client.deleteObject(request);
		System.out.println("删除对象结束");
	}

	public static void uploadFile() {
		AmazonS3Client client = getAmazonS3Client();
		// photos/user_base/img/1499769520971943.jpg
		String fileName = "1499769520971943.jpg";
		String localPath = "f:" + fileName;
		String key = "photos/user_base/img/" + fileName;

		File file = new File(localPath);

		// 默认添加public权限
		client.putObject(new PutObjectRequest(bucketName, key, file).withCannedAcl(CannedAccessControlList.PublicRead));
		// client.putObject(new PutObjectRequest(bucketName, key, file));
		System.out.println("创建对象结束");
		watchObject();
	}

	/**
	 * 将文件上传到指定的目录
	 * 上传同一个key 则替换原文件
	 * 
	 * @param key
	 *            目录
	 * @param localPath
	 *            待上传的文件
	 */
	public static void uploadFile(String key, String localPath) {
		AmazonS3Client client = getAmazonS3Client();

		File file = new File(localPath);

		// 默认添加public权限
		client.putObject(new PutObjectRequest(bucketName, key, file).withCannedAcl(CannedAccessControlList.PublicRead));
		System.out.println("创建对象结束");
	}

	public static void uploadFileThread(String key, String localPath) {
		// for test
		// String fileName = "1499769520971942.jpg";
		// localPath = "f:" + fileName;
		// key = "photos/raiders/img/" + fileName;

		AmazonS3Client client = getAmazonS3Client();
		TransferManager tm = new TransferManager(client);
		TransferManagerConfiguration conf = tm.getConfiguration();

		long threshold = 4 * 1024 * 1024;
		conf.setMultipartUploadThreshold(threshold);
		tm.setConfiguration(conf);
		Upload upload = tm.upload(bucketName, key, new File(localPath));
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
		client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
		System.out.print("创建对象成功");
	}

}

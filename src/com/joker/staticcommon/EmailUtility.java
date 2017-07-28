package com.joker.staticcommon;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class EmailUtility {
	/**
	 * 发送邮件
	 * 
	 * @return 成功返回true，失败返回false
	 */
	static Logger logger = LogManager.getLogger(EmailUtility.class.getName());

	/**
	 * 发送邮件
	 * 
	 * @param destAddress
	 *            邮箱地址
	 * @param subject
	 *            title
	 * @param content
	 *            内容
	 * @return
	 */
	public static boolean sendMail(String destAddress, String subject, String content) {
		return sendMail(destAddress, subject, content, null);
	}

	/**
	 * 发送邮件附加附件
	 * 
	 * @param destAddress
	 *            邮箱地址
	 * @param subject
	 *            title
	 * @param content
	 *            内容
	 * @param attachment
	 *            附件所在的文件路径
	 * @return
	 */
	public static boolean sendMail(String destAddress, String subject, String content, String attachment) {
		EmailUtility.MailInfo mail = new EmailUtility.MailInfo();
		mail.setTo(destAddress);
		mail.setSubject(subject);
		mail.setContent(content);
		mail.setAttachment(attachment);
		return sendMail(mail);
	}

	public static void main(String[] args) {
		// sendMailDirectly("sunny.liu@sidlu.com", "测试", "测试内容");
		// sendMail("xiangruimx@163.com", "测试", "测试内容");
		// sendMail("xiangruimx@yahoo.com", "测试", "测试内容");
		// sendMail("joker.xiang@sidlu.com", "测试", "测试内容");
		sendMail("xiangrui0805@gmail.com", "测试", "测试内容",
				"C:/tomcat/apache-tomcat-8.0.41-honghu/webapps/photos/message/m1/150099085020713.jpg,C:/tomcat/apache-tomcat-8.0.41-honghu/webapps/photos/message/m1/1500990857320350.jpg");
	}

	/**
	 * 发送邮件
	 * 
	 * @return 成功返回true，失败返回false
	 */
	public static boolean sendMailDirectly(String destAddress, String subject, String content) {
		logger.info("sendMailDirectly");
		return sendMailDirectly(destAddress, subject, content, null);
	}

	public static boolean sendMailDirectly(String destAddress, String subject, String content, String attachment) {
		EmailUtility.MailInfo mail = new EmailUtility.MailInfo();
		mail.setTo(destAddress);
		mail.setSubject(subject);
		mail.setContent(content);
		mail.setAttachment(attachment);
		return sendMailDirectly(mail);
	}

	public static boolean sendMail(MailInfo mail) {
		MutiThreadSender sender = new EmailUtility.MutiThreadSender(mail);
		Thread thread = new Thread(sender);
		thread.start();
		return true;
	}

	static Properties props;

	public static boolean sendMailDirectly(final MailInfo mail) {
		// 构造mail session
		System.out.println(mail.getTo());
		System.out.println(mail.getContent());
		Properties props = new Properties();
		// props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.host", "smtp.mailgun.org");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mail.getUsername(), mail.getPassword());
			}
		});

		try {
			// 构造MimeMessage并设定基本的值，创建消息对象
			MimeMessage msg = new MimeMessage(session);
			// 设置消息内容
			msg.setFrom(new InternetAddress(mail.getFrom(), MimeUtility.encodeText(mail.getFromName(), "UTF-8", "B")));
			// 把邮件地址映射到Internet地址上
			InternetAddress[] address = { new InternetAddress(mail.getTo()) };
			/**
			 * setRecipient（Message.RecipientType type, Address
			 * address），用于设置邮件的接收者。<br>
			 * 有两个参数，第一个参数是接收者的类型，第二个参数是接收者。<br>
			 * 接收者类型可以是Message.RecipientType .TO，Message
			 * .RecipientType.CC和Message.RecipientType.BCC，TO表示主要接收人，CC表示抄送人
			 * ，BCC表示秘密抄送人。接收者与发送者一样，通常使用InternetAddress的对象。
			 */
			msg.setRecipients(Message.RecipientType.TO, address);
			// 设置邮件的标题
			String subject = mail.getSubject();
			try {
				subject = MimeUtility.encodeText(subject, "UTF-8", "B");
			} catch (UnsupportedEncodingException e) {
				logger.error(String.format("mail.smtp.host:%s", mail.getHost()), e);
				e.printStackTrace();
			}
			msg.setSubject(subject);
			// 构造Multipart
			Multipart mp = new MimeMultipart();

			// 向Multipart添加正文
			MimeBodyPart mbpContent = new MimeBodyPart();
			// 设置邮件内容(纯文本格式)
			// mbpContent.setText(mail.getContent(), "text/html;charset=utf-8");
			// 设置邮件内容(HTML格式)
			mbpContent.setContent(mail.getContent().replaceAll("(\r\n|\r|\n|\n\r)", "<br>"), "text/html;charset=utf-8");
			// 向MimeMessage添加（Multipart代表正文）
			mp.addBodyPart(mbpContent);
			// 向Multipart添加MimeMessage

			// 添加附件的内容
			if (!StringUtility.isNullOrEmpty(mail.getAttachment())) {
				if (mail.getAttachment().contains(",")) {
					// 以逗号分隔多个文件
					String imgSrcArr[] = mail.getAttachment().split(",");
					for (String imgSrc : imgSrcArr) {
						if (!StringUtility.isNullOrEmpty(imgSrc)) {
							File attachment = new File(imgSrc);
							if (attachment.exists()) {
								BodyPart attachmentBodyPart = new MimeBodyPart();
								DataSource source = new FileDataSource(attachment);
								attachmentBodyPart.setDataHandler(new DataHandler(source));
								// MimeUtility.encodeWord可以避免文件名乱码
								attachmentBodyPart.setFileName(MimeUtility.encodeText(attachment.getName(), "UTF-8", "B"));
								//
								attachmentBodyPart.setFileName(MimeUtility.encodeWord(attachment.getName()));
								mp.addBodyPart(attachmentBodyPart);
							}
						}
					}
				} else {
					File attachment = new File(mail.getAttachment());
					if (attachment.exists()) {
						BodyPart attachmentBodyPart = new MimeBodyPart();
						DataSource source = new FileDataSource(attachment);
						attachmentBodyPart.setDataHandler(new DataHandler(source));
						// MimeUtility.encodeWord可以避免文件名乱码
						attachmentBodyPart.setFileName(MimeUtility.encodeText(attachment.getName(), "UTF-8", "B"));
						//
						attachmentBodyPart.setFileName(MimeUtility.encodeWord(attachment.getName()));
						mp.addBodyPart(attachmentBodyPart);
					}
				}
			}

			msg.setContent(mp);
			// 设置邮件发送的时间。
			msg.setSentDate(new Date());
			// 发送邮件
			Transport.send(msg);
		} catch (Exception e) {
			logger.error(String.format("mail.smtp.host:%s, to:%s", mail.getHost(), mail.getTo()), e);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static class MutiThreadSender implements Runnable {

		private MailInfo mail;

		public MutiThreadSender(MailInfo mail) {
			this.mail = mail;
		}

		@Override
		public void run() {
			try {
				EmailUtility.sendMailDirectly(this.mail);
			} catch (Exception e) {
				logger.error("", e);
				e.printStackTrace();
			}
		}

	}

	public static class MailInfo {

		// Gmail
		// String from = CommonRunTimeConfig.EmailConfig.from;//
		// "notification@honghuworld.com"; 发件人
		// String fromName = CommonRunTimeConfig.EmailConfig.fromName;// "鸿鹄世界";
		// String host = CommonRunTimeConfig.EmailConfig.host;//
		// "smtp.gmail.com"; smtp主机
		// String port = CommonRunTimeConfig.EmailConfig.port;// "465"; //
		// smtp端口
		// String username = CommonRunTimeConfig.EmailConfig.username;//
		// "notification@honghuworld.com"; 用户名
		// String password = CommonRunTimeConfig.EmailConfig.password;//
		// "kiqqhvojkuiifzao"; 密码

		// mailgun
		String from = "notification@mg.honghuworld.com";
		// 发件人
		String fromName = "鸿鹄世界";
		String host = "smtp.mailgun.org";
		// smtp主机
		String port = "465"; // smtp端口
		String username = "notification@mg.honghuworld.com";
		// 用户名
		String password = "J6hYbx6K";
		// 密码

		String to = ""; // 收件人
		String subject = ""; // 邮件主题
		String content = ""; // 邮件正文
		String attachment;

		public MailInfo() {
		}

		public MailInfo(String from, String host, String username, String password) {
			this.from = from;
			this.host = host;
			this.username = username;
			this.password = password;
		}

		public String getFrom() {
			return from;
		}

		public String getFromName() {
			return fromName;
		}

		public String getHost() {
			return host;
		}

		public String getPort() {
			return port;
		}

		public void setPort(String port) {
			this.port = port;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getAttachment() {
			return attachment;
		}

		public void setAttachment(String attachment) {
			this.attachment = attachment;
		}
	}

}

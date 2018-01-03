package com.joker.normal;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PaseXML {

	public static void main(String[] args) {
		test1();

	}

	private static void test1() {
		try {
			File f = new File("f:/chinese.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			NodeList nl = doc.getElementsByTagName("string");
			for (int i = 0; i < nl.getLength(); i++) {
				Node item = doc.getElementsByTagName("string").item(i);
				NamedNodeMap bookmap = item.getAttributes();
				for (int j = 0; j < bookmap.getLength(); j++) {
					Node node = bookmap.item(j);
					System.out.print(node.getNodeValue() + "<>");
				}
				System.out.println(doc.getElementsByTagName("string").item(i).getFirstChild().getNodeValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

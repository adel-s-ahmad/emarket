package com.emarket.order.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.emarket.order.models.Order;
import com.emarket.order.models.OrderStatus;

public class CSVHelper {
	
	public static String TYPE = "text/csv";
	static String[] HEADERs = { "country", "sku", "name", "stock_change" };

	public static boolean hasCSVFormat(MultipartFile file) {
		if (!TYPE.equals(file.getContentType())) {
			return false;
		}
		return true;
	}

	public static List<Order> csvToOrders(InputStream is) {
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			CSVParser csvParser = new CSVParser(fileReader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader()
						.withIgnoreHeaderCase().withTrim());) {
			List<Order> orders = new ArrayList<Order>();
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecords) {
				Order order = new Order();
				order.setCountry(csvRecord.get(HEADERs[0]));
				order.setSku(csvRecord.get(HEADERs[1]));
				order.setName(csvRecord.get(HEADERs[2]));
				order.setQuantity(Long.parseLong(csvRecord.get(HEADERs[3])));
				order.setStatus(OrderStatus.Pending.toString());
				order.setCreatedAt(new Date());
				orders.add(order);
				
			}
			return orders;
		} catch (IOException e) {
			throw new RuntimeException("failed to parse CSV file: " + e.getMessage());
		}
	}
}

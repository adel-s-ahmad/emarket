package com.emarket.product.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order implements Serializable{
	private int id;
	private String sku;
	private String name;
	private String country;
	private long quantity;
	private String source;
	private String status;
	private String notes;
	private Date createdAt;
}

package br.com.bikeapi.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Bike implements Serializable{
	
	private static final long serialVersionUID = -1301934965199664570L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * Serial code to identify the bike
	 */
	@Column(unique = true)
	private String chassi;
	
	/**
	 * Describes the bike's model
	 */
	@Column
	private String model;
	
	/**
	 * Describes the color of the bike
	 */
	@Column
	private String color;
	
	/**
	 * Assigns if the bike belongs to the client
	 */
	@Column
	private Boolean companyProperty;

}

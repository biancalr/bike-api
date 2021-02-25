package br.com.bikeapi.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Rent implements Serializable{

	private static final long serialVersionUID = -6705960860735616347L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private LocalDateTime rentDate;
	
	@Column
	private LocalDateTime expectedReturnDate;
	
	@Column
	private LocalDateTime returnedDateTime;
	
	@Column(nullable = false)
	private Integer rentHoursDuration;
	
	@Column
	private String customerEmail;
	
	@JoinColumn(name = "id_client", nullable = false)
	@OneToOne(fetch = FetchType.LAZY)
	private Client client;
	
	@JoinColumn(name = "id_bike", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Bike bike;
	
}

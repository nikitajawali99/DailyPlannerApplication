package com.dailyplanner.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users_roles")
public class UsersRoles {

	@Id
	@Column(nullable = false, unique = true)
	private Long user_id;

	@Column(nullable = false, unique = true)
	private Long role_id;

}

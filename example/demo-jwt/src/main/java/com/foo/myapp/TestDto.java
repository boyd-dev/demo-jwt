package com.foo.myapp;

import java.util.Objects;


public class TestDto {
	
	private String name;
	
	private Integer age;
	
	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
	@Override
	public int hashCode() {		
		return Objects.hash(this.name + String.valueOf(this.age));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}		
		TestDto t = (TestDto) obj;
		return (this.name + String.valueOf(this.age)).equals(t.name + String.valueOf(t.age));		
	}
	
	
	@Override
	public String toString() {		
		return this.name + ":" + this.age;
	}
}

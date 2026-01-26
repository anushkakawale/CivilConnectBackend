package com.example.CivicConnect.exception;

public class InvalidRequestException extends RuntimeException {
	public InvalidRequestException(String message) {
		super(message);
	}
}

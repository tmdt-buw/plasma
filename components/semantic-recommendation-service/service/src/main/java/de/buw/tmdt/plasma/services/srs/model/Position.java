package de.buw.tmdt.plasma.services.srs.model;


public class Position {
	private double xCoordinate;
	private double yCoordinate;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	private Position() {
		xCoordinate = yCoordinate = 0;
	}

	public Position(double xCoordinate, double yCoordinate) {
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	public double getXCoordinate() {
		return xCoordinate;
	}

	public double getYCoordinate() {
		return yCoordinate;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Position\""
		       + ", \"xCoordinate\":\"" + xCoordinate + '"'
		       + ", \"yCoordinate\":\"" + yCoordinate + '"'
		       + '}';
	}
}

package de.buw.tmdt.plasma.services.kgs.database.api;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface GraphDBDriverFactory extends Serializable {

	@NotNull
	GraphDBDriver getGraphDBDriver();
}
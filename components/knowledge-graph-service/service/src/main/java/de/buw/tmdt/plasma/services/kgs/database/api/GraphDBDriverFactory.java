package de.buw.tmdt.plasma.services.kgs.database.api;

import org.jetbrains.annotations.NotNull;


public interface GraphDBDriverFactory {

	@NotNull
	GraphDBDriver getGraphDBDriver();
}
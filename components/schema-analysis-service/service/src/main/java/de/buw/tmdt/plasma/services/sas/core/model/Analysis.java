package de.buw.tmdt.plasma.services.sas.core.model;

import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
public class Analysis {

	public UUID getUuid() {
		return uuid;
	}

	@Id
	@Column(nullable = false, unique = true, length = 16)
	protected UUID uuid;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Node> subAnalyses;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Node result;

	protected Analysis() {

	}

	public Analysis(UUID uuid) {
		this.uuid = uuid;
		this.subAnalyses = new ArrayList<>();
	}

	public void addSubAnalysis(Node node) {
		subAnalyses.add(node);
	}

	public void setResult(Node result) {
		this.result = result;
	}

	public List<Node> getSubAnalyses() {
		return Collections.unmodifiableList(subAnalyses);
	}

	public Node getResult() {
		return result;
	}
}

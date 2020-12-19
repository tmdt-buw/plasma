package de.buw.tmdt.plasma.services.dms.core.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class ModelBase {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;

	@SuppressWarnings("unused - hibernate constructor")
	protected ModelBase() {

	}

	protected ModelBase(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModelBase modelBase = (ModelBase) o;
		return super.equals(o) || Objects.equals(id, modelBase.id) && id != null;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"ModelBase\""
		       + ", \"id\":\"" + id + '"'
		       + '}';
	}
}

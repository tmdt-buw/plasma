package de.buw.tmdt.plasma.services.dms.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.services.dms.core.database.CombinedModelPersistenceConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@DynamicUpdate
@Entity
@Table(name = "modelings")
public class Modeling {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @NotNull
    private String id;

    private String name;

    @Column(length = 4000)
    private String description;

    private ZonedDateTime created;

    private String dataId;

    @Column()
    @ElementCollection
    @NotNull
    private List<String> selectedOntologies;

    //The value is only set after the schema analysis
    // Map <Version, CombinedModel>
    @SuppressWarnings("JpaAttributeTypeInspection") // will be custom serialized
    @ElementCollection
    @Convert(converter = CombinedModelPersistenceConverter.class)
    @Lob
    @JsonIgnore
    @NotNull
    private List<@NotNull CombinedModel> combinedModelVersions = new ArrayList<>();

    @Column(nullable = false)
    @JsonIgnore
    private int versionPointer;

    public Modeling() {
        this.versionPointer = 0;
        this.id = UUID.randomUUID().toString();
        this.selectedOntologies = new ArrayList<>();
        this.created = ZonedDateTime.now();
    }

    public Modeling(@NotNull String id) {
        this();
        this.id = id;
    }

    public Modeling(@NotNull String id, @Nullable String name, @Nullable String description, @NotNull ZonedDateTime created, @Nullable String dataId) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.created = created;
        this.dataId = dataId;
    }

    @NotNull
    @JsonIgnore
    public List<CombinedModel> getAllCombinedModels() {
        return this.combinedModelVersions;
    }

    public void setAllCombinedModels(@NotNull List<CombinedModel> models) {
        combinedModelVersions.clear();
        models.stream().filter(Objects::nonNull).forEach(combinedModelVersions::add);
        versionPointer = combinedModelVersions.size();
    }

    public void pushCombinedModel(@NotNull CombinedModel model) {
        if (versionPointer >= 1 && versionPointer != combinedModelVersions.size()) {
            if (combinedModelVersions.size() > versionPointer - 1) {
                combinedModelVersions.subList(versionPointer, combinedModelVersions.size()).clear();
            }
        }
        combinedModelVersions.add(model);
        versionPointer++;
    }

    @JsonIgnore
    @Transient
    @NotNull
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") // all values are not null
    public CombinedModel getCurrentModel() {
        if (versionPointer == 0 || combinedModelVersions.isEmpty()) {
            throw new UnsupportedOperationException("No model available.");
        }
        try {
            return combinedModelVersions.get(versionPointer - 1);
        } catch (IndexOutOfBoundsException e) {
            // somehow the versions got messed up, reset to latest version
            versionPointer = combinedModelVersions.size();
            return combinedModelVersions.get(versionPointer - 1);
        }
    }

    public void popCombinedModel() throws UnsupportedOperationException {
        if (versionPointer == 0) {
            throw new UnsupportedOperationException("No model available.");
        }
        if (versionPointer == 1) {
            throw new UnsupportedOperationException("Already on first version.");
        }
        --this.versionPointer;
    }

    @NotNull
    public CombinedModel restoreCombinedModel() throws UnsupportedOperationException {
        if (versionPointer == this.combinedModelVersions.size()) {
            throw new UnsupportedOperationException("Already on latest model.");
        }
        ++this.versionPointer;
        return getCurrentModel();
    }

    public List<String> getSelectedOntologies() {
        return selectedOntologies;
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"DataSourceModel\""
                + ", \"@super\":" + super.toString()
                + ", \"currentVersion\":" + versionPointer
                + ", \"maxVersion\":" + combinedModelVersions.size()
                + '}';
    }

    @NotNull
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDataId() {
        return dataId;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCreated() {
        return created;
    }
}
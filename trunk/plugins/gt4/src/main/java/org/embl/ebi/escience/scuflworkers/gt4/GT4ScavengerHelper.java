/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, the University of Chicago
 */
package org.embl.ebi.escience.scuflworkers.gt4;

import gov.nih.nci.cadsr.umlproject.domain.*;
import gov.nih.nci.cagrid.cadsr.client.*; 

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;


import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for handling GT4 scavengers.
 * 
 * @author Wei Tan
 */
public class GT4ScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(GT4ScavengerHelper.class);
	
	//give an initial value to classNameArray
	//the data is retrieved at 8:30 am, June 16th, 2008
	public static String []classNameArray = {
		"A2Conjugate", 
		"A2Experiment", "A2LP4Parameters", "A2Plate", "A2Sample", 
		"A2Spot42", "A2SpotData", "A2SpotSetup", "A2SpotsStatistics", 
		"A2StandardCurve", "A2Well", "AbsoluteCodingSchemeVersionReference", "AbsoluteCodingSchemeVersionReferenceList", 
		"AbsoluteNeutrophilCount", "AbstractAdverseEventTerm", "AbstractArrayData", "AbstractBioMaterial", 
		"AbstractCaArrayEntity", "AbstractCaArrayObject", "AbstractCancerModel", "AbstractCharacteristic", 
		"AbstractContact", "AbstractDataColumn", "AbstractDesignElement", "AbstractDomainObject", 
		"Abstraction", "AbstractMeddraDomain", "AbstractProbe", "AbstractProbeAnnotation", 
		"AbstractStudyDisease", "Accession", "AccessionCharacteristics", "AccessRights", 
		"AcquisitionProcedure", "ActionSuccessor", "ActivationMethod", "ActiveIngredient", 
		"ActiveObservation", "ActiveSite", "Activity", "ActivityRelationship", 
		"ActivitySummary", "ActivityType", "AcuteGraftVersusHostDisease", "Add", 
		"AdditionalFindings", "AdditionalInformation", "AdditionalOrganismName", "Address", 
		"AddToReferenceParameters", "AdjacencyMatrix", "AdministeredComponent", "AdministeredComponentClassSchemeItem", 
		"AdministeredComponentContact", "AdministeredDrug", "AdverseEvent", "AdverseEventAttribution", 
		"AdverseEventCtcTerm", "AdverseEventDetail", "AdverseEventMeddraLowLevelTerm", "AdverseEventNotification", 
		"AdverseEventResponseDescription", "AdverseEventTherapy", "AeTerminology", "Agent", 
		"AgentOccurrence", "AgentSynonym", "AgentTarget", "Alignment", 
		"Aliquot", "Allergy", "AlternateProteinHit", "Amendment", 
		"AmendmentApproval", "AnalysisGroup", "AnalysisGroupResult", "AnalysisParameters", 
		"AnalysisRecord", "AnalysisRoutine", "AnalysisRun", "AnalysisRunSet", 
		"AnalysisVariable", "Analyte", "AnalyteProcessingStep", "AnatomicEntity", 
		"AnatomicSite", "Animal", "AnimalAvailability", "AnimalDistributor", 
		"AnimalModel", "AnnotatableEntity", "AnnotatableEvent", "Annotation", 
		"AnnotationEventParameters", "AnnotationManager", "AnnotationOfAnnotation", "AnnotationSet", 
		"Anomaly", "Antibody", "Antigen", "Application", 
		"ApplicationContext", "AppliedParameter", "ApprovalStatus", "AracneParameter", 
		"Array", "ArrayDataType", "ArrayDesign", "ArrayDesignDetails", 
		"ArrayGroup", "ArrayManufacture", "ArrayManufactureDeviation", "ArrayReporter", 
		"ArrayReporterCytogeneticLocation", "ArrayReporterPhysicalLocation", "Assay", "AssayDataPoint", 
		"AssayType", "Assembly", "Assessment", "AssessmentRelationship", 
		"AssociatedElement", "AssociatedFile", "AssociatedObservationWrapper", "Association", 
		"AssociativeFunction", "Atom", "Attachment", "AttributeSetDescriptor", 
		"AttributeTypeMetadata", "Audit", "AuditEvent", "AuditEventDetails", 
		"AuditEventLog", "AuditEventQueryLog", "Availability", "AveragedSpotInformation", 
		"BACCloneReporter", "BaselineData", "BaselineHistoryPE", "BaselineStage", 
		"BasicHistologicGrade", "BehavioralMeasure", "BibliographicReference", "BindingSite", 
		"BioAssay", "BioAssayCreation", "BioAssayData", "BioAssayDataCluster", 
		"BioAssayDatum", "BioAssayDimension", "BioAssayMap", "BioAssayMapping", 
		"BioAssayTreatment", "BiocartaMap", "BiocartaReport", "BiocartaSource", 
		"BioDataCube", "BioDataTuples", "BioDataValues", "BioEvent", 
		"Biohazard", "BiologicalProcess", "BioMaterial", "BioMaterial_package", 
		"BioMaterialMeasurement", "Biopolymer", "BioSample", "BioSequence", 
		"BioSource", "BlackoutDate", "BloodContact", "BooleanColumn", 
		"BooleanParameter", "BreastCancerAccessionCharacteristics", "BreastCancerBiomarkers", "BreastCancerTNMFinding", 
		"BreastNegativeSurgicalMargin", "BreastPositiveSurgicalMargin", "BreastSpecimenCharacteristics", "BreastSurgicalPathologySpecimen", 
		"CaArrayFile", "CalciumBindingRegion", "CalculatedMeasurement", "CalculatedMeasurementProtocol", 
		"Calculation", "CalculationResult", "Canceled", "CancerModel", 
		"CancerResearchGroup", "CancerStage", "CancerTNMFinding", "Capacity", 
		"CarbonNanotube", "CarbonNanotubeComposition", "CarcinogenExposure", "CarcinogenicIntervention", 
		"caseDetail", "CaseReportForm", "Caspase3Activation", "CatalystActivity", 
		"Category", "CategoryObservation", "CategorySummaryExport", "CategorySummaryReportRow", 
		"CD", "CellLine", "CellLysateFinding", "cells", 
		"CellSpecimen", "CellSpecimenRequirement", "CellSpecimenReviewParameters", "CellViability", 
		"CentralLaboratory", "CFU_GM", "cghSamples", "Chain", 
		"Change", "ChangedGenes", "Channel", "Characterization", 
		"CharacterizationProtocol", "CharacterParameter", "CheckInCheckOutEventParameter", "ChemicalAssociation", 
		"ChemicalClass", "ChemicalStressor", "ChemicalStressorProtocol", "ChemicalTreatment", 
		"Chemotaxis", "Chemotherapy", "ChemotherapyData", "ChimerismSample", 
		"ChromatogramPoint", "Chromosome", "ChromosomeMap", "ChronicGraftVersusHostDisease", 
		"Chunk", "Circle", "citation", "ClassComparisonAnalysis", 
		"ClassComparisonAnalysisFinding", "ClassificationScheme", "ClassificationSchemeItem", "ClassificationSchemeItemRelationship", 
		"ClassificationSchemeRelationship", "ClassMembership", "ClassSchemeClassSchemeItem", "ClinicalAssessment", 
		"ClinicalFinding", "ClinicalMarker", "ClinicalReport", "ClinicalResult", 
		"ClinicalTrial", "ClinicalTrialProtocol", "ClinicalTrialSite", "ClinicalTrialSponsor", 
		"ClinicalTrialSubject", "Clone", "CloneRelativeLocation", "Cluster", 
		"CNSAccessionCharacteristics", "CNSCarcinoma", "CNSHistologicGrade", "CNSNeoplasmHistologicType", 
		"CNSSpecimenCharacteristics", "Coagulation", "codedContext", "codedEntry", 
		"CodedNodeSetImpl", "CodeSequence", "CodingSchemeRendering", "CodingSchemeRenderingList", 
		"CodingSchemeSummary", "CodingSchemeSummaryList", "CodingSchemeTag", "CodingSchemeTagList", 
		"CodingSchemeURNorName", "CodingSchemeVersionOrTag", "CodingSchemeVersionStatus", "Coefficients", 
		"Cohort", "CoiledCoil", "CoiledCoilRegion", "Coils", 
		"CollaborativeStaging", "CollectionEventParameters", "CollectionProtocol", "CollectionProtocolEvent", 
		"CollectionProtocolRegistration", "CollisionCell", "ColorectalAccessionCharacteristics", "ColorectalCancerTNMFinding", 
		"ColorectalHistologicGrade", "ColorectalSpecimenCharacteristics", "CometScores", "comment", 
		"Comment", "Comments", "CommonLookup", "Comorbidity", 
		"ComparativeMarkerSelectionParameterSet", "ComparativeMarkerSelectionResultCollection", "ComplementActivation", "Complex", 
		"ComplexComponent", "ComplexComposition", "ComponentConcept", "ComponentLevel", 
		"ComponentName", "ComposingElement", "CompositeCompositeMap", "CompositeGroup", 
		"CompositePosition", "CompositeSequence", "CompositeSequenceDimension", "CompositeSequenceSummary", 
		"CompositionallyBiasedRegion", "Compound", "CompoundMeasurement", "ConcentrationUnit", 
		"Concept", "ConceptClassification", "conceptCode", "ConceptCodes", 
		"ConceptDerivationRule", "ConceptDescriptor", "conceptProperty", "ConceptReference", 
		"ConceptReferenceList", "ConceptReferent", "concepts", "ConceptualDomain", 
		"ConcomitantMedication", "ConcomitantMedicationAttribution", "ConcomitantMedicationDetail", "ConcomitantProcedure", 
		"ConcomitantProcedureDetail", "Condition", "Conditional", "Conditionality", 
		"ConditionGroup", "ConditionMessage", "ConfidenceIndicator", "ConsensusClusteringParameterSet", 
		"ConsensusClusterResultCollection", "ConsensusIdentifierData", "ConsensusMatrix", "ConsensusMatrixRow", 
		"Constraint", "Contact", "ContactCommunication", "ContactDetails", 
		"ContactInfo", "ContactMechanismBasedRecipient", "ContactMechanismType", "Container", 
		"ContainerInfo", "ContainerType", "Context", "context", 
		"Contour", "Control", "ControlGenes", "ControlledVocabularyAnnotation", 
		"Coordinate", "CopyNumberFinding", "CourseAgent", "CourseAgentAttribution", 
		"CourseDate", "CrossLink", "Ctc", "CtcCategory", 
		"CtcGrade", "CtcTerm", "CtepStudyDisease", "Culture", 
		"CultureProtocol", "CurationData", "CustomProperties", "CutaneousMelanoma", 
		"CutaneousMelanomaAccessionCharacteristics", "CutaneousMelanomaAdditionalFindings", "CutaneousMelanomaNegativeSurgicalMargin", "CutaneousMelanomaNeoplasmHistologicType", 
		"CutaneousMelanomaPositiveSurgicalMargin", "CutaneousMelanomaSpecimenCharacteristics", "CutaneousMelanomaSurgicalPathologySpecimen", "CutaneousMelanomaTNMFinding", 
		"Cycle", "Cytoband", "CytobandPhysicalLocation", "CytogeneticLocation", 
		"CytokineInduction", "Cytotoxicity", "Data", "Database", 
		"DatabaseCrossReference", "DatabaseEntry", "DatabaseSearch", "DatabaseSearchParameters", 
		"DatabaseSearchParametersOntologyEntry", "DataElement", "DataElementConcept", "DataElementConceptRelationship", 
		"DataElementDerivation", "DataElementRelationship", "DataFile", "DataFileLimsFile", 
		"DataItem", "DataRetrievalRequest", "DataServiceInstance", "DataSet", 
		"DataSource", "DataStatus", "DataType", "Datum", 
		"dbxref", "dc", "DeathSummary", "DefinedParameter", 
		"Definition", "definition", "DefinitionClassSchemeItem", "DeliveryStatus", 
		"Delta", "Demographics", "Dendrimer", "DendrimerComposition", 
		"Department", "DerivationType", "DerivedArrayData", "DerivedBioAssay", 
		"DerivedBioAssayData", "DerivedBioAssays", "DerivedDataElement", "DerivedDataFile", 
		"DerivedDatum", "DerivedDNACopySegment", "DerivedSignal", "DescLogicConcept", 
		"DescLogicConceptVocabularyName", "describable", "Describable", "Description", 
		"Designation", "DesignationClassSchemeItem", "DesignElement", "DesignElementDimension", 
		"DesignElementGroup", "DesignElementList", "DesignElementMap", "DesignElementMapping", 
		"Detection", "DeviceAttribution", "DeviceOperator", "Diagnosis", 
		"DICOMImageReference", "DiffFoldChangeFinding", "Dige", "DigeGel", 
		"DigeSpotDatabaseSearch", "DigeSpotMassSpec", "Dimension", "DirectedAcyclicGraph", 
		"Disease", "DiseaseAttribution", "DiseaseCategory", "DiseaseEvaluation", 
		"DiseaseEvaluationDetail", "DiseaseExtent", "DiseaseHistory", "DiseaseOntology", 
		"DiseaseOntologyRelationship", "DiseaseOutcome", "DiseaseResponse", "DiseaseTerm", 
		"DiseaseTerminology", "DisposalEventParameters", "DistanceUnit", "DistantSite", 
		"DistributedItem", "Distribution", "DistributionProtocol", "DistributionProtocolAssignment", 
		"DistributionProtocolOrganization", "DisulfideBond", "DNA", "DNABindingRegion", 
		"DNAcopyAssays", "DNAcopyParameter", "DNASpecimen", "Documentation", 
		"Domain", "DomainDescriptor", "DomainName", "Donor", 
		"DonorInfo", "DonorRecipient", "Dose", "DoubleColumn", 
		"DoubleParameter", "Drug", "DrugSurgeryData", "Duration", 
		"EdgeProperties", "EditActionDate", "Electrospray", "Ellipse", 
		"EmbeddedEventParameters", "Emulsion", "EmulsionComposition", "Encapsulation", 
		"EndpointCode", "EngineeredGene", "EnsemblGene", "EnsemblPeptide", 
		"EnsemblTranscript", "EntityAccession", "entityDescription", "EntityMap", 
		"EntityName", "EntrezGene", "EnucleationInvasiveProstateCarcinoma", "EnucleationProstateSpecimenCharacteristics", 
		"EnucleationProstateSurgicalPathologySpecimen", "EnumeratedValueDomain", "EnvironmentalFactor", "EnzymeInduction", 
		"Epoch", "EpochDelta", "Equipment", "Error", 
		"Event", "EventEntity", "EventEntitySet", "EventParameters", 
		"EventRecords", "Evidence", "EvidenceCode", "EvidenceKind", 
		"EVSDescLogicConceptSearchParams", "EVSHistoryRecordsSearchParams", "EVSMetaThesaurusSearchParams", "EVSSourceSearchParams", 
		"Examination", "ExcisionCutaneousMelanomaSpecimenCharacteristics", "ExcisionCutaneousMelanomaSurgicalPathologySpecimen", "Execution", 
		"ExocrinePancreasAccessionCharacteristics", "ExocrinePancreasSpecimenCharacteristics", "ExocrinePancreaticCancerTNMFinding", "Exon", 
		"ExonArrayReporter", "ExonProbeAnnotation", "ExpectedValue", "ExpeditedAdverseEventReport", 
		"Experiment", "Experiment2DGelList", "Experiment2DLiquidChromatography", "Experiment2DLiquidChromatography1stSetup", 
		"Experiment2DLiquidChromatography2ndSetup", "ExperimentalFactor", "ExperimentalFeatures", "ExperimentalStructure", 
		"ExperimentContact", "ExperimentDesign", "ExperimentRun", "ExperimentTo2DGel", 
		"ExperimentToDatabaseSearch", "ExperimentToDige", "ExperimentToMassSpec", "Exponent", 
		"ExportStatus", "ExpressedSequenceTag", "ExpressionArrayReporter", "ExpressionData", 
		"ExpressionFeature", "ExpressionLevelDesc", "ExpressionProbeAnnotation", "ExpressoParameter", 
		"Extendable", "ExtensionDescription", "ExtensionDescriptionList", "ExternalIdentifier", 
		"ExternalReference", "Extract", "Facility", "Factor", 
		"FactorValue", "Failed", "FamilyHistory", "FastaFiles", 
		"FastaSequences", "Feature", "FeatureData", "FeatureDefect", 
		"FeatureDimension", "FeatureExtraction", "FeatureGroup", "FeatureInformation", 
		"FeatureLocation", "FeatureReporterMap", "FeatureType", "FemaleReproductiveCharacteristic", 
		"Ffas", "Fiducial", "File", "FileType", 
		"Filters", "Finding", "FirstCourseRadiation", "FirstCourseTreatmentSummary", 
		"FISHFinding", "FixedEventParameters", "FloatColumn", "FloatingPointQuantity", 
		"FloatParameter", "FluidSpecimen", "FluidSpecimenRequirement", "FluidSpecimenReviewEventParameters", 
		"Fold", "Folder", "Followup", "Form", 
		"FormatType", "FormElement", "Fraction", "FractionAnalyteSteps", 
		"Fractions", "FrozenEventParameters", "FuhrmanNuclearGrade", "Fullerene", 
		"FullereneComposition", "Function", "functionalCategory", "FunctionalDNADomain", 
		"FunctionalizingEntity", "FunctionalProteinDomain", "FunctionalRole", "GbmDrugs", 
		"GbmPathology", "GbmSlide", "GbmSurgery", "Gel", 
		"Gel2d", "GelImage", "GelImageType", "GelPlug", 
		"GelSpot", "GelSpotList", "GelStatus", "Genbank", 
		"GenBankAccession", "GenBankmRNA", "GenBankProtein", "Gene", 
		"gene_product", "GeneAgentAssociation", "GeneAlias", "GeneAnnotation", 
		"GeneBiomarker", "GeneCategoryExport", "GeneCategoryMatrix", "GeneCategoryMatrixRow", 
		"GeneCategoryReportRow", "GeneCytogeneticLocation", "GeneDelivery", "GeneDiseaseAssociation", 
		"GeneExprReporter", "GeneFunction", "GeneFunctionAssociation", "GeneGenomicIdentifier", 
		"GeneNeighborsParameterSet", "GeneOntology", "GeneOntologyRelationship", "GenePhysicalLocation", 
		"GenePubmedSummary", "GeneRelativeLocation", "GeneReporterAnnotation", "GenericArray", 
		"GenericReporter", "GeneticAlteration", "GeneVersion", "GenomeEncodedEntity", 
		"GenomicIdentifier", "GenomicIdentifierSet", "GenomicIdentifierSolution", "GenomicSegment", 
		"Genotype", "GenotypeDiagnosis", "GenotypeFinding", "GenotypeSummary", 
		"Genus", "GeometricShape", "GleasonHistologicGrade", "GleasonHistopathologicGrade", 
		"GlycosylationSite", "GominerGene", "GominerTerm", "GOTerm", 
		"Grade", "GradientStep", "Graft", "GraftVersusHostDisease", 
		"GraftVersusHostDiseaseOutcome", "Group", "GroupRoleContext", "Hap2Allele", 
		"Haplotype", "Hardware", "HardwareApplication", "header", 
		"HealthcareSite", "HealthCareSite", "HealthcareSiteInvestigator", "HealthcareSiteParticipant", 
		"HealthcareSiteParticipantRole", "Helix", "HematologyChemistry", "Hemolysis", 
		"HemTransplantEndocrineProcedure", "Hexapole", "HierarchicalCluster", "HierarchicalClusteringMage", 
		"HierarchicalClusteringParameter", "HierarchicalClusterNode", "HighLevelGroupTerm", "HighLevelTerm", 
		"HistologicGrade", "Histology", "HistopathologicGrade", "Histopathology", 
		"HistopathologyGrade", "History", "HistoryRecord", "Hmmpfam", 
		"HomologAlignment", "HomologousAssociation", "HormoneTherapy", "Hybridization", 
		"HybridizationData", "Hypothesis", "Identifiable", "IdentificationScheme", 
		"IdentifiedPathologyReport", "IdentifiedPatient", "IdentifiedSection", "Identifier", 
		"IHCFinding", "II", "Image", "ImageAcquisition", 
		"ImageAnnotation", "ImageContrastAgent", "ImageDataItem", "ImageReference", 
		"ImageType", "ImageView", "ImageViewModifier", "Imaging", 
		"ImagingFunction", "ImagingObservation", "ImagingObservationCharacteristic", "ImmuneCellFunction", 
		"Immunologic", "Immunotherapy", "Immunotoxicity", "ImmunoToxicity", 
		"INDHolder", "InducedMutation", "InitiatorMethionine", "Input", 
		"InputFile", "Instance", "Institution", "Instruction", 
		"instruction", "Instrument", "InstrumentConfiguration", "InstrumentType", 
		"IntegerColumn", "IntegerParameter", "IntegerQuantity", "IntegrationType", 
		"Interaction", "InternetSource", "Intron", "InvasiveBreastCarcinoma", 
		"InvasiveBreastCarcinomaNeoplasmHistologicType", "InvasiveColorectalCarcinoma", "InvasiveColorectalCarcinomaNeoplasmHistologicType", "InvasiveExocrinePancreaticCarcinoma", 
		"InvasiveExocrinePancreaticCarcinomaNeoplasmHistologicType", "InvasiveKidneyCarcinoma", "InvasiveKidneyCarcinomaNeoplasmHistologicType", "InvasiveLungCarcinoma", 
		"InvasiveLungCarcinomaNeoplasmHistologicType", "InvasiveProstateCarcinoma", "InvasiveProstateCarcinomaNeoplasmHistologicType", "Investigation", 
		"InvestigationalNewDrug", "Investigator", "InvestigatorHeldIND", "InvitroCharacterization", 
		"InvivoResult", "Invoker", "IonSource", "IonTrap", 
		"JaxInfo", "JpegImage", "Jpred", "Keyword", 
		"KidneyAccessionCharacteristics", "KidneyAdditionalFindings", "KidneyCancerTNMFinding", "KidneySpecimenCharacteristics", 
		"Lab", "LabeledExtract", "LabFile", "LabGeneral", 
		"LabGroup", "LabMember", "Laboratory", "LaboratoryEquipment", 
		"LaboratoryFinding", "LaboratoryPersonnel", "LaboratoryProject", "LaboratoryResult", 
		"LaboratorySamplePlate", "LaboratoryStorageDevice", "LaboratoryTest", "LabSpecial", 
		"LabValue", "Lambda", "LesionDescription", "LesionEvaluation", 
		"LeukocyteProliferation", "LevelOfExpressionIHCFinding", "LexBIGServiceImpl", "Library", 
		"LimsFile", "Lineage", "Linkage", "LinkType", 
		"LipidMoietyBindingRegion", "Liposome", "LiposomeComposition", "LiquidChromatographyColumn", 
		"List", "ListProcessing", "LiteratureRelationship", "LoadStatus", 
		"localId", "LocalNameList", "Location", "Log", 
		"LogEntry", "LogicalProbe", "LogLevel", "LOHFinding", 
		"LongColumn", "LongParameter", "LongTermFU", "LossOfExpressionIHCFinding", 
		"LowComplexityRegion", "LowLevelTerm", "Lsid", "LungAccessionCharacteristics", 
		"LungCancerTNMFinding", "LungDrugs", "LungExam", "LungNeoplasm", 
		"LungPathology", "LungSlide", "LungSpecimenCharacteristics", "LungSurgery", 
		"Macroprocess", "MAGE", "Maldi", "ManufactureLIMS", 
		"ManufactureLIMSBiomaterial", "Manufacturer", "Map", "Mapping", 
		"Marker", "MarkerAlias", "MarkerPhysicalLocation", "MarkerRelativeLocation", 
		"MarkerResult", "MarketingAuthorization", "MarketingAuthorizationHolderManufacturerDistributor", "MascotScores", 
		"MassQuery", "MassSpecDatabaseSearch", "MassSpecExperiment", "MassSpecMachine", 
		"MassSpecMassSpecFraction", "MassUnit", "MatchingParameters", "Material", 
		"MaterialSource", "MaterialType", "MathFile", "MeasuredBioAssay", 
		"MeasuredBioAssayData", "MeasuredBioAssays", "MeasuredSignal", "Measurement", 
		"MeasurementCharacteristic", "MeasurementProtocol", "MeasureUnit", "Meddra", 
		"MeddraStudyDisease", "MedicalDevice", "MedicinalProduct", "MessengerRNA", 
		"MetadataProperty", "MetadataPropertyList", "MetalIonBindingSite", "MetalParticle", 
		"MetalParticleComposition", "MetastasisSite", "MetastaticDiseaseSite", "MetaThesaurusConcept", 
		"Method", "MethodParameter", "Methylation", "MethylationDnaSequence", 
		"MethylationSite", "MethylationSiteMeasurement", "Microarray", "MicroArrayData", 
		"MicroarrayEventRecords", "MicroarraySet", "MismatchInformation", "Missed", 
		"MobilePhaseComponent", "Model", "ModelGroup1", "Modeller", 
		"ModelSection", "ModelStructure", "Modifications", "ModificationType", 
		"ModifiedResidue", "Module", "ModuleDescription", "ModuleDescriptionList", 
		"MolecularSpecimen", "MolecularSpecimenRequirement", "MolecularSpecimenReviewParameters", "MolecularWeight", 
		"Morpholino", "Morphology", "Mouse", "mRNAGenomicIdentifier", 
		"MS2Runs", "Msi", "MultiPoint", "MultiProcessParameters", 
		"MutagenesisSite", "MutationIdentifier", "MutationVariation", "MZAnalysis", 
		"MzAssays", "MzSpectrum", "MZXMLSubFiles", "Name", 
		"NameAndValue", "NameAndValueList", "NameValueType", "Nanoparticle", 
		"NanoparticleDatabaseElement", "NanoparticleEntity", "NanoparticleSample", "NanoparticleStudy", 
		"NeedleBiopsyInvasiveProstateCarcinoma", "NeedleBiopsyProstateSpecimenCharacteristics", "NeedleBiopsyProstateSurgicalPathologySpecimen", "NegativeControl", 
		"Neoplasm", "NeoplasmHistologicType", "NeoplasmHistopathologicType", "NKCellCytotoxicActivity", 
		"Node", "NodeContents", "NodeValue", "Noise", 
		"Nomenclature", "NonCancerDirectedSurgery", "NonConsecutiveResidues", "NonenumeratedValueDomain", 
		"NonTerminalResidue", "NonverifiedSamples", "NormalChromosome", "NormalizeInvariantSetParameter", 
		"NormalizeMethodParameter", "NormalizeQuantilesRobustParameter", "NotApplicable", "NotificationBodyContent", 
		"NottinghamHistologicGrade", "NottinghamHistopathologicGrade", "NucleicAcidPhysicalLocation", "NucleicAcidSequence", 
		"NucleotideBindingRegion", "NucleotidePhosphateBindingRegion", "Null", "NumericalRangeConstraint", 
		"NumericMeasurement", "NumericOID", "ObjectClass", "ObjectClassRelationship", 
		"Observation", "ObservationConcept", "ObservationData", "ObservationProtocol", 
		"ObservationRelationship", "ObservationState", "ObservedThing", "ObservedThingToObservationConnection", 
		"ObservedThingToObservedThingRelationship", "ObservedThingToOntologyElementRelationship", "Occurred", "OctaveFile", 
		"OddsRatio", "OffTreatment", "OMIM", "OnStudy", 
		"OntologyDimension", "OntologyElement", "OntologyElementSlot", "OntologyElementSlotSet", 
		"OntologyElementToOntologyElementRelationship", "OntologyEntry", "OntologyGroup", "OntologyGroupToOntologyGroupRelationship", 
		"OrderItem", "OrderOfNodeTraversal", "OrderSet", "Organ", 
		"Organelle", "Organism", "OrganismName", "Organization", 
		"OrganizationAssignedIdentifier", "OrganizationHeldIND", "OrganOntology", "OrganOntologyRelationship", 
		"OrthologousGene", "OtherAnalyte", "OtherAnalyteAnalyteProcessingSteps", "OtherAnalyteOntologyEntry", 
		"OtherAnalyteProcessingSteps", "OtherAnalyteProcessingStepsOntologyEntry", "OtherBreastCancerHistologicGrade", "OtherBreastCancerHistopathologicGrade", 
		"OtherCause", "OtherCauseAttribution", "OtherChemicalAssociation", "OtherFunction", 
		"OtherFunctionalizingEntity", "OtherIonisation", "OtherIonisationOntologyEntry", "OtherMZAnalysis", 
		"OtherMZAnalysisOntologyEntry", "OtherNanoparticleEntity", "OtherProcedure", "OtherTarget", 
		"OtherTherapy", "Outcome", "OutcomeType", "Output", 
		"OutputFile", "OvarianDrugs", "OvarianExam", "OvarianNeoplasm", 
		"OvarianPathology", "OvarianSlide", "OvarianSurgery", "OxidativeBurst", 
		"OxidativeStress", "Package", "Parameter", "Parameterizable", 
		"ParameterizableApplication", "ParameterList", "ParameterValue", "Participant", 
		"ParticipantEligibilityAnswer", "ParticipantHistory", "ParticipantMedicalIdentifier", "Participation", 
		"ParticleComposition", "Party", "PartyRole", "Password", 
		"Pathology", "PathologyEventRecords", "PathologyReport", "Pathway", 
		"Patient", "PatientIdentifier", "PatientVisit", "Pdb", 
		"Pdbblast", "Peak", "PeakDetectionParameters", "PeakList", 
		"PeakLocation", "PeakSpecificChromint", "PedigreeGraph", "PedigreeNode", 
		"Peptide", "PeptideHit", "PeptideHitModifications", "PeptideHitOntologyEntry", 
		"PeptideHitProteinHit", "PeptideMembers", "PeptidesBase", "Percentile", 
		"PercentX", "PerformingLaboratory", "Period", "PeriodDelta", 
		"PermissibleValue", "Person", "person", "PersonContact", 
		"PersonName", "Personnel", "PersonOccupation", "PETEvaluation", 
		"PETEvaluationDetail", "Phagocytosis", "PharmaceuticalProduct", "Pharmacokinetics", 
		"Phase", "Phenomenon", "PhenomenonType", "Phenotype", 
		"PhenotypeDiagnosis", "PhysicalArrayDesign", "PhysicalBioAssay", "PhysicalCharacterization", 
		"PhysicalEntity", "PhysicalExam", "PhysicalLocation", "PhysicalParticipant", 
		"PhysicalPosition", "PhysicalProbe", "PhysicalState", "Physician", 
		"Place", "PlannedActivity", "PlannedActivityDelta", "PlannedCalendar", 
		"PlannedCalendarDelta", "PlannedEmailNotification", "PlannedNotification", "PlasmaProteinBinding", 
		"Platelet", "PlateletAggregation", "PloidyStruct", "Point", 
		"PolyAlleleFrequency", "PolyGenoFrequency", "Polyline", "Polymer", 
		"PolymerComposition", "Polymorphism", "PolynomialDegree", "PolypectomySpecimenCharacteristics", 
		"Population", "PopulationFrequency", "Portion", "Position", 
		"PositionDelta", "PositiveControl", "PostAdverseEventStatus", "PreExistingCondition", 
		"PreferredTerm", "Prep", "PreprocessDatasetParameterSet", "PresentAbsent", 
		"presentation", "PresentationThresholds", "PrimaryDiseasePresentation", "PrimarySiteSurgery", 
		"Primer", "PrimerPairs", "PrincipleComponentAnalysis", "PrincipleComponentAnalysisFinding", 
		"PriorTherapy", "PriorTherapyAgent", "Privilege", "ProbabilityMap", 
		"Probe", "ProbeGroup", "Procedure", "ProcedureDataFile", 
		"ProcedureDataType", "ProcedureEventParameters", "ProcedureSample", "ProcedureSampleType", 
		"ProcedureType", "ProcedureTypeProtocol", "ProcedureUnit", "Process", 
		"ProcessDataFile", "ProcessDetails", "ProcessEquipment", "ProcessLog", 
		"ProcessLogLimsFile", "PROcessParameter", "ProcessSample", "ProcessState", 
		"ProcessStatus", "ProcessType", "ProductInfused", "Profile", 
		"Project", "Project2Sample", "Project2SNP", "ProjectDataFile", 
		"Projection", "ProjectPersonnel", "ProjectProcedure", "ProjectReport", 
		"ProjectReportLimsFile", "ProjectSample", "Promoter", "Propeptide", 
		"properties", "Property", "property", "PropertyChange", 
		"PropertyDescriptor", "propertyId", "propertyLink", "propertyQualifier", 
		"propertyQualifierId", "PropertyValue", "ProstateAccessionCharacteristics", "ProstateAdditionalFindings", 
		"ProstateCancerTNMFinding", "ProstateSpecimenCharacteristics", "ProstateSurgicalPathologySpecimen", "ProtectionElement", 
		"ProtectionGroup", "ProtectionGroupRoleContext", "Protein", "Protein2MMDB", 
		"ProteinAlias", "ProteinBiomarker", "ProteinDige", "ProteinDomain", 
		"ProteinEncodingGeneFeature", "ProteinFeature", "ProteinGenomicIdentifier", "ProteinGroupMembers", 
		"ProteinGroups", "ProteinHit", "ProteinHomolog", "ProteinName", 
		"ProteinProphetFiles", "Proteins", "ProteinSequence", "ProteinSpotSet", 
		"ProteinStructure", "ProteinSubunit", "ProteomicsEventRecords", "Protocol", 
		"Protocol_package", "ProtocolAction", "ProtocolApplication", "ProtocolAssociation", 
		"ProtocolDefinition", "ProtocolFile", "ProtocolFormsSet", "ProtocolFormsTemplate", 
		"ProtocolLimsFile", "ProtocolStatus", "ProtocolStep", "ProtSequences", 
		"Provenance", "Publication", "PublicationSource", "PublicationStatus", 
		"PubMed", "Purity", "PValue", "QaReport", 
		"Quadrupole", "Qualifier", "QualitativeEvaluation", "Quantile", 
		"QuantitationType", "QuantitationTypeDimension", "QuantitationTypeMap", "QuantitationTypeMapping", 
		"Quantity", "QuantityInCount", "QuantityInGram", "QuantityInMicrogram", 
		"QuantityInMilliliter", "QuantityUnit", "QuantSummaries", "QuantumDot", 
		"QuantumDotComposition", "Query", "Question", "QuestionCondition", 
		"QuestionConditionComponents", "QuestionRepetition", "Radiation", "RadiationAdministration", 
		"RadiationAttribution", "RadiationIntervention", "RadiationTherapy", "RadicalProstatectomyGleasonHistologicGrade", 
		"RadicalProstatectomyGleasonHistopathologicGrade", "RadicalProstatectomyInvasiveProstateCarcinoma", "RadicalProstatectomyProstateNegativeSurgicalMargin", "RadicalProstatectomyProstatePositiveSurgicalMargin", 
		"RadicalProstatectomyProstateSpecimenCharacteristics", "RadicalProstatectomyProstateSurgicalMargin", "RadicalProstatectomyProstateSurgicalPathologySpecimen", "Ratio", 
		"RawArrayData", "RawSample", "RBC", "Reaction", 
		"ReceivedEventParameters", "Receptor", "Recipient", "RecipientDisease", 
		"RecipientLaboratoryFinding", "Recurrence", "ReexcisionCutaneousMelanomaSpecimenCharacteristics", "ReexcisionCutaneousMelanomaSurgicalPathologySpecimen", 
		"Reference", "ReferenceChemical", "ReferencedAnnotation", "ReferencedCalculation", 
		"ReferenceDocument", "ReferenceEntity", "ReferenceGene", "ReferenceLink", 
		"ReferenceProtein", "ReferenceRNA", "ReferenceSequence", "ReferenceSpotInformation", 
		"RefSeqmRNA", "RefSeqProtein", "RefseqProtein", "RegionalDistantSurgery", 
		"RegionalLymphNodeSurgery", "RegionalNodeLymphadenectomyCutaneousMelanomaSpecimenCharacteristics", "RegionalNodeLymphadenectomyCutaneousMelanomaSurgicalPathologySpecimen", "Registration", 
		"Regulation", "Regulator", "RegulatoryElement", "RegulatoryElementType", 
		"RelatedGelItem", "RelatedGelItemProteinHit", "RelationshipObservation", "RelationshipType", 
		"RelativeLocation", "RelativeRecurringBlackout", "Remove", "RenderingDetail", 
		"Reorder", "Repeat", "Report", "ReportDefinition", 
		"ReportDelivery", "ReportDeliveryDefinition", "Reporter", "ReporterBasedAnalysis", 
		"ReporterCompositeMap", "ReporterDimension", "ReporterGroup", "ReporterPosition", 
		"ReportMandatoryFieldDefinition", "ReportPerson", "ReportSection", "ReportStatus", 
		"ReportVersion", "RepositoryInfo", "Representation", "ResearchInstitutionSource", 
		"ResearchStaff", "ResectionColonSpecimenCharacteristics", "Resolution", "ResolvedConceptReference", 
		"ResolvedConceptReferenceList", "ResponseAssessment", "ReviewEventParameters", "RFile", 
		"RNA", "RnaAnnotation", "RnaExperiment", "RnaReport", 
		"RnaResult", "Role", "RoleBasedRecipient", "RouteOfAdministration", 
		"RoutineAdverseEventReport", "RoutineOption", "Run", "RunProtocol", 
		"RunSampleContainer", "SAEReportPreExistingCondition", "SAEReportPriorTherapy", "Sample", 
		"SampleAnalyteProcessingSteps", "SampleCategory", "SampleComposition", "SampleContainer", 
		"SampleLocation", "SampleLog", "SampleManagement", "SampleOrigin", 
		"SamplePlate", "SampleProvider", "SampleSampleOrigin", "SampleSOP", 
		"SampleSOPFile", "SampleType", "Scalar", "ScannerProperties", 
		"Scheduled", "ScheduledActivity", "ScheduledActivityState", "ScheduledCalendar", 
		"ScheduledEmailNotification", "ScheduledEvent", "ScheduledNotification", "ScheduledStudySegment", 
		"ScheduledTimeLineEvent", "ScreeningResult", "SecondaryParticipantIdentifier", "SecondarySpecimenIdentier", 
		"SecondarySpecimenIdentifier", "SecondaryStructure", "Security", "SecurityGroup", 
		"Seg", "SegmentType", "Selenocysteine", "SemanticMetadata", 
		"SemanticType", "SeqFeature", "SeqFeatureLocation", "Sequence", 
		"SequenceAnnotation", "SequenceConflict", "SequencePosition", "SequenceVariant", 
		"SequestScores", "Series", "Sets", "SexDistribution", 
		"Shape", "ShortColumn", "ShortParameter", "ShortSequenceMotif", 
		"Signalp", "SignalPeptide", "Silo", "SimpleName", 
		"SimpleTimePeriod", "Site", "SiteInvestigator", "Size", 
		"SkyCase", "SkyCase_header", "skyCellHeader", "skyChromosome", 
		"SkyDataFile", "SkyDataFileSet", "skyFrag", "SkyMageMappings", 
		"Slide", "SmallMolecule", "SmallMoleculeEntity", "SNP", 
		"SNP2Allele", "SNP2Gene", "SNPAnalysisGroup", "SnpAnalysisResult", 
		"SnpAnnotation", "SNPAnnotation", "SnpArrayExperiment", "SNPArrayReporter", 
		"SNPAssay", "SNPAssociationAnalysis", "SNPAssociationFinding", "SNPCytogeneticLocation", 
		"snpFinding", "SNPFrequencyFinding", "SNPMapping", "SNPPanel", 
		"SNPPhysicalLocation", "SNPProbeAnnotation", "SnpResult", "SocialHistory", 
		"Software", "SoftwareApplication", "Solubility", "SomaticMutationFinding", 
		"SomCluster", "SomClusteringParameter", "SortContext", "SortDescription", 
		"SortDescriptionList", "SortOption", "SortOptionList", "Source", 
		"source", "SourceMeasurement", "SourceMeasurementProtocol", "SourceReference", 
		"Span", "SpatialCoordinate", "SpecializedQuantitationType", "Species", 
		"species", "SpeciesType", "SpecificDateBlackout", "Specimen", 
		"SpecimenAcquisition", "SpecimenArray", "SpecimenArrayContent", "SpecimenArrayType", 
		"SpecimenBasedAnalysis", "SpecimenBasedFinding", "SpecimenBasedMolecularFinding", "SpecimenCharacteristics", 
		"SpecimenCollection", "SpecimenCollectionGroup", "SpecimenEventParameters", "SpecimenProtocol", 
		"SpecimenRequirement", "SpectraData", "SpliceVariant", "SpontaneousMutation", 
		"Spot", "SpotAnalyteProcessingSteps", "SpotDetectionParameters", "SpotDige", 
		"SpotImage", "SpotMap", "SpotMapGroup", "SpotRatio", 
		"SpotSet", "SpotSetSpot", "SpunEventParameters", "StainingMethod", 
		"StandardQuantitationType", "StaticRelationship", "Status", "StorageContainer", 
		"StorageContainerCapacity", "StorageContainerDetails", "StorageDevice", "StorageElement", 
		"StorageType", "Strain", "Strand", "Strength", 
		"StringColumn", "StringParameter", "StructuralAlignment", "STS", 
		"Study", "StudyAgent", "StudyCoordinatingCenter", "StudyFundingSponsor", 
		"StudyInvestigator", "StudyObservation", "StudyOrganization", "StudyParticipant", 
		"StudyParticipantAssignment", "StudyPersonnel", "StudyProtocol", "StudyPublication", 
		"StudySegment", "StudySegmentDelta", "StudySite", "StudyStressor", 
		"StudySubject", "StudySubjectAssignment", "StudyTherapy", "StudyTimePoint", 
		"Subject", "SubjectAssignment", "SubjectGroup", "SubProject", 
		"SubstanceAdministration", "Subsystem", "SubsystemCell", "Summation", 
		"SuppGenotype", "SupportedElement", "SupportedElementList", "Surface", 
		"SurfaceChemistry", "SurfaceGroup", "Surgery", "SurgeryAttribution", 
		"SurgeryIntervention", "SurgeryTreatment", "SurgicalMargin", "SurgicalPathologySpecimen", 
		"Swissprot", "SynopticSurgicalPathologyReport", "SystemAssignedIdentifier", "SystemOrganClass", 
		"TaggingProcess", "TandemSequenceData", "Target", "TargetedModification", 
		"TargetingFunction", "Taxon", "TemperatureUnit", "Term", 
		"term", "term2term", "TermBasedCharacteristic", "TermSource", 
		"text", "TextAnnotation", "TextMeasurement", "ThawEventParameters", 
		"TherapeuticFunction", "TherapeuticProcedure", "Therapy", "ThreeDimensionalSize", 
		"ThreeDimensionalTumorSize", "ThreeDSpatialCoordinate", "Threshold", "Tile", 
		"TimeCourse", "TimeOfFlight", "TimePeriod", "TimePoint", 
		"TimeRecord", "TimeScaleUnit", "TimeSeries", "TimeUnit", 
		"Tissue", "TissueSpecimen", "TissueSpecimenRequirement", "TissueSpecimenReviewEventParameters", 
		"Tmhmm", "TopologicalDomain", "Topology", "TotalGenes", 
		"TotalProteinContent", "Toxicity", "Trace2Genotype", "TransanalDiskExcisionSpecimenCharacteristics", 
		"Transcript", "TranscriptAnnotation", "TranscriptArrayReporter", "TranscriptPhysicalLocation", 
		"TransferEventParameters", "Transformation", "Transgene", "TransitPeptide", 
		"TransmembraneRegion", "TransurethralResectionInvasiveProstateCarcinoma", "TransurethralResectionProstateSpecimenCharacteristics", "TransurethralResectionProstateSurgicalPathologySpecimen", 
		"TreatedAnalyte", "TreatedAnalyteAnalyteProcessingSteps", "Treatment", "TreatmentAssignment", 
		"TreatmentInformation", "TreatmentSchedule", "TreeNode", "TrialDataProvenance", 
		"TriggerAction", "tsBoolean", "tsCaseIgnoreDirectoryString", "tsCaseIgnoreIA5String", 
		"tsCaseSensitiveDirectoryString", "tsCaseSensitiveIA5String", "tsInteger", "tsTimestamp", 
		"tsURN", "TumorCode", "TumorSpecimenBiopsy", "Turn", 
		"TwoDLiquidChromatography1stDimension", "TwoDLiquidChromatography2ndDimension", "TwoDSpatialCoordinate", "TwoDSpotDatabaseSearch", 
		"TwoDSpotMassSpec", "TypeEnumerationMetadata", "UCUM", "UMLAssociationMetadata", 
		"UMLAttributeMetadata", "UMLClassMetadata", "UMLGeneralizationMetadata", "UMLPackageMetadata", 
		"UnclassifiedAgent", "UnclassifiedAgentTarget", "UnclassifiedLinkage", "UniGene", 
		"UnigeneTissueSource", "UniprotAccession", "UniProtKB", "UniprotkbAccession", 
		"Unit", "UnsureResidue", "URL", "URLSourceReference", 
		"User", "UserComments", "UserGroup", "UserOrganization", 
		"UserProtectionElement", "UserRoleContext", "ValidValue", "Value", 
		"ValueDomain", "ValueDomainPermissibleValue", "ValueDomainRelationship", "ValueMeaning", 
		"ValuesNearToCutoff", "VariationFinding", "VariationReporter", "version", 
		"versionable", "versionableAndDescribable", "versionReference", "Vocabulary", 
		"Volume", "VolumeUnit", "WebImageReference", "WebServicesSourceReference", 
		"WeekDayBlackout", "Window", "Xenograft", "XmlReport", 
		"XTandemScores", "YeastModel", "ZincFingerRegion", "Zone", 
		"ZoneDefect", "ZoneGroup", "ZoneLayout"
	};
	
	public GT4ScavengerHelper(){
		
		try{
			org.apache.axis.utils.ClassUtils.setDefaultClassLoader(CaDSRServiceClient.class.getClassLoader()) ;	
		}
		catch (Exception e) {
			 e.printStackTrace();
			 }
		
	
	}
	
	
	

	public String getScavengerDescription() {
		
		return "Add new caGrid(GT4) scavenger...";
	}
	 public ActionListener getListener(ScavengerTree theScavenger) {
	        final ScavengerTree s = theScavenger;
	        return new ActionListener() {
	            public void actionPerformed(ActionEvent ae) {

	                final JDialog dialog = new JDialog(s.getContainingFrame(),
	                        "Add Your Custom Service Query", true);
	                final GT4ScavengerDialog gtd = new GT4ScavengerDialog();
	                dialog.getContentPane().add(gtd);
	                JButton accept = new JButton("Send Service Query");
	                JButton cancel = new JButton("Cancel");
	                JButton updateCaDSRData = new JButton("Update caDSR Data");
	                updateCaDSRData.setToolTipText("Get an updated UML Class list from caDSR services. \n" +
	                		"This operation may take a few minutes depending on network status. ");
	            
	                gtd.add(accept);
	                //gtd.add(new JLabel("Send Service Query to Index Service"));
	                gtd.add(cancel);
	                gtd.add(updateCaDSRData);
	                gtd.addQuery.addActionListener(new ActionListener(){
	                	 public void actionPerformed(ActionEvent ae3) {
	                		 if (dialog.isVisible()) {
	                			 if(gtd.q_count<gtd.q_size){
	                				 gtd.queryList[gtd.q_count].setVisible(true);
	                				 gtd.queryValue[gtd.q_count].setVisible(true);
	                				 gtd.validate();
	                				 gtd.q_count++;
	                				 System.out.println("Add a New Query-- now q_count == " + gtd.q_count);
	                			 }
	                		 }
	                	 }
	                	
	                });
	                gtd.removeQuery.addActionListener(new ActionListener(){
	                	 public void actionPerformed(ActionEvent ae4) {
	                		 if (dialog.isVisible()) {
	                			 if(gtd.q_count>1){
	                				 gtd.queryList[gtd.q_count-1].setVisible(false);
	                				 gtd.queryValue[gtd.q_count-1].setVisible(false);
	                				 gtd.validate();
	                				 gtd.q_count--;
	                				 System.out.println("Remove a New Query-- now q_count == " + gtd.q_count);
	                			 }
	                		 }
	                	 }
	                	
	                });
	                
	                
	                //listeners for service query criteria
	                //do some prompt when possible, like if service query criteria is "operation class", "operation input"
	                //or "operation output", retrieve all classes from caDSR
	                for(int i=0;i<gtd.q_size;i++){
	                	final JComboBox qValue =gtd.queryValue[i];
                		final JComboBox qCriteria = gtd.queryList[i];
	                	gtd.queryList[i].addActionListener(new ActionListener () {   		
	                		public void actionPerformed(ActionEvent ae4) {
		                		 if (dialog.isVisible()) {
		                			 String qString =  (String) qCriteria.getSelectedItem();
		                			 System.out.println(qString+"--Selected");
		                			 //if the selection should be prompted with caDSR data
		                			 if(qString.equals("Operation Class")||qString.equals("Operation Input")
		                					 ||qString.equals("Operation Output") ){
		                				 //TODO get the classNameList by invoking caDSR; add to another thread!!
		                				 qValue.setModel(new DefaultComboBoxModel(GT4ScavengerHelper.classNameArray));
		                					        
		                				}
		                			 //if the selection should NOT be prompted with caDSR data
		                			 else {
		                				 //keep the combo box empty and editible
		                				 String [] emptyValue = {};
		                				 qValue.setModel(new DefaultComboBoxModel(emptyValue));
		                				 //in the future more hints can be given to assist the users
		                				 
		                			 }		                				 
		                				 qValue.validate();		             
		                				 gtd.validate();
		                			 }
		                		
		                		
		                		 }
		                	 });
	                }
	                	
	                
	                accept.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent ae2) {
	                        if (dialog.isVisible()) {
	                        	String indexURL = "";
	                            String queryCriteria = "";
	                            String queryValue = "";
	                            ServiceQuery squery = null;
	                            
	                            if (gtd.getIndexServiceURL().equals(""))
	                            	//default index URL
	                                indexURL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	                            else
	                                indexURL = gtd.getIndexServiceURL();
	                            
	                            //gather service queries
	                            int [] flag = new int[gtd.q_count];
	                            int count = 0;
	                            for (int i=0;i<gtd.q_count;i++){
	                            	if(!gtd.getQueryCriteria(i).equals("None")&&!gtd.getQueryValue(i).equals("")){
	                            		count ++ ;
	                            		flag[i]=1;
	                            	}
	        
	                            }
	                            ServiceQuery [] sq= null;
	                            if(count>0){
	                            	sq = new ServiceQuery[count];
	 	                            int j = 0;
	 	                            for (int i=0;i<gtd.q_count;i++){
	 	                            	if(flag[i]==1){
	 	                            		sq[j++] = new ServiceQuery(gtd.getQueryCriteria(i),gtd.getQueryValue(i));
	 	                            		System.out.println("Adding Query: "+ sq[j-1].queryCriteria + "  = " + sq[j-1].queryValue);
	 	                          		
	 	                            	}	
	                            }
	                           
	                            }
	                            
	                            
	                            try {
	                            	final String url = indexURL;
	                            	final ServiceQuery[] f_sq = sq;
	                            	Thread t = new Thread("Adding GT4 scavenger") {
	            						public void run() {
	            							s.scavengingStarting("Adding GT4 scavenger");
	            							try {
	            								GT4Scavenger gs = new GT4Scavenger(url, f_sq);
	                                            s.addScavenger(gs);
	            							} catch (ScavengerCreationException sce) {
	            								JOptionPane.showMessageDialog(s.getContainingFrame(), "Unable to create scavenger!\n" + sce.getMessage(),
	            										"Exception!", JOptionPane.ERROR_MESSAGE);
	            							}
	            							s.scavengingDone();
	            						}
	                            	};
	                            	t.start();
	                            } catch (Exception e) {
	                                JOptionPane
	                                        .showMessageDialog(s.getContainingFrame(),
	                                                "Unable to create scavenger!\n"
	                                                        + e.getMessage(),
	                                                "Exception!",
	                                                JOptionPane.ERROR_MESSAGE);
	                                logger.error("Exception thrown:", e);
	                            } finally {
	                                dialog.setVisible(false);
	                                dialog.dispose();
	                            }
	                        }
	                    }
	                });
	                cancel.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent ae2) {
	                        if (dialog.isVisible()) {
	                            dialog.setVisible(false);
	                            dialog.dispose();
	                        }
	                    }
	                });
	                updateCaDSRData.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent ae2) {
	                        if (dialog.isVisible()) {
	                        	Thread t = new Thread("Updating caDSR Metadata.") {
            						public void run() {
	                        	
		                          //TODO: update the value of classNameArray
			                        	 ArrayList classNameList = new ArrayList();
		            					 Project[] projs = null;
		            					 CaDSRServiceClient cadsr  =null;
		            					 UMLPackageMetadata[] packs = null;
		            					 UMLClassMetadata[] classes = null;
		            					 System.out.println("===========Updating caDSR Metadata================");
		            					 
		            					 //Note: the try-catch module should be with fine granularity
		            					
		            					 try {
		            						 
		            						  cadsr = new CaDSRServiceClient(
		            								 "http://cagrid-service.nci.nih.gov:8080/wsrf/services/cagrid/CaDSRService");		                					            
		            					     projs = cadsr.findAllProjects();
		            					 }
		            					 catch (Exception e) {
		            						 e.printStackTrace();
		            						 }
		            					 
		            					     if(projs !=null){
		    					            	for (int i = 0; i<projs.length;i++){
		    					            		Project project = projs[i];
		    					            		//System.out.println("\n"+ project.getShortName());
		    					            		if(!project.getShortName().equals("BRIDG")&&!project.getShortName().equals("C3PR")){
		    					            			try {
		    					            				packs = cadsr.findPackagesInProject(project);
		    					            			}
		    					            			catch (Exception e) {
		   		                						 e.printStackTrace();
		   		                						 }
		    						            		if(packs !=null){
		    						            			for(int j= 0;j<packs.length;j++){
		    						            				UMLPackageMetadata pack = packs[j];
		    						            				//System.out.println("\t-" + pack.getName());
		    						            				try {
		    						            					 classes = cadsr.findClassesInPackage(project, pack.getName());
		    						            				}
		    						            				catch (Exception e) {
		    				                						 e.printStackTrace();
		    				                						 }
		    						            				if(classes !=null){
		    						            					for (int k=0;k<classes.length;k++){
		    						            						UMLClassMetadata clazz = classes [k];
		    						            						//System.out.println("\t\t-"+clazz.getName());
		    						            						if(!classNameList.contains(clazz.getName()))
		    						            							//classNameList is updated here!
		    						            							classNameList.add((String)clazz.getName());
		    						            						else {
		    						            							//System.out.println("Duplicated Class Name Found.");
		    						            							}
		    						            						}
		    						            					}
		    						            				}
		    						            			}
		    						            		}
		    					            		}
		    					            	}
		        					             
		        					            String [] classNameArray;
		        					            //if the retrived class name list is not empty, update the static datatype classNameArray
		            					        if(!classNameList.isEmpty()){
		            					        	classNameArray = (String[]) classNameList.toArray(new String[0]);
		                					        Arrays.sort(classNameArray,String.CASE_INSENSITIVE_ORDER);		                					       
		                					        System.out.println("=========Classes Names Without Duplications=============");
		                					        for(int i=0;i<classNameArray.length;i++){
		                					        	System.out.println(classNameArray[i]);
		                					        }
		                					        
		                					        GT4ScavengerHelper.classNameArray  = classNameArray;
		                					        //System.out.println("caDSR data is updated, now there are " + classNameArray.length + "UMLClasses.");
		                					        JOptionPane.showMessageDialog(s.getContainingFrame(), "caDSR data has been  updated. \n Now there are " + classNameArray.length + " UMLClasses in the list.", null, JOptionPane.INFORMATION_MESSAGE);
		                					        
		            					        }
		            					        else{
		            					        	//the current value of the GT4ScavengerHelper.classNameArray is not updated
		            					        	//System.out.println("Empty class name list retrived, so classNameArray is not updated!");
		            					        	JOptionPane.showMessageDialog(s.getContainingFrame(),"Empty class name list retrived, so classNameArray is not updated!", null, JOptionPane.INFORMATION_MESSAGE);
		            					        }
			                        	
			                        }
            						};
            						t.start();
            					
	                        }
	                    }
	                });
	                dialog.setResizable(false);
	                dialog.getContentPane().add(gtd);
	                dialog.setLocationRelativeTo(null);
	                dialog.pack();
	                dialog.setVisible(true);

	            }
	        };
	    }

	
	/* The old, simple GUI 
	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String indexLocation = (String) JOptionPane
						.showInputDialog(s.getContainingFrame(),
								"Address of the GT4 Service Index?",
								"Discovery location", JOptionPane.QUESTION_MESSAGE,
								null, null, "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService");
				if (indexLocation != null) {
					Runnable r = new Runnable() {
						public void run() {
							s.scavengingStarting("Processing Index");
							try {
								s.addScavenger(new GT4Scavenger(
										indexLocation));
							} catch (ScavengerCreationException sce) {
								JOptionPane
										.showMessageDialog(s
												.getContainingFrame(),
												"Unable to create scavenger!\n"
														+ sce.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
							s.scavengingDone();
						}
					};
					new Thread(r, "GT4 Scavenger processing").start();
				}
			}
		};
	}
	*/

	/**
	 * returns the default Scavenger set
	 */
	   public synchronized Set<Scavenger> getDefaults() {
			Set<Scavenger> result = new HashSet<Scavenger>();
			String urlList = System.getProperty("taverna.defaultgt4");
			if (urlList != null) {
				String[] urls = urlList.split("\\s*,\\s*");
				for (String url : urls) {
					try {
						result.add(new GT4Scavenger(url,null));
					} catch (ScavengerCreationException e) {
						logger.error("Error creating BiomobyScavenger for " + url, e);
					}
				}
			}
			return result;
		}
		
		public Set<Scavenger> getFromModel(ScuflModel model) {
			Set<Scavenger> result = new HashSet<Scavenger>();
			List<String> existingLocations = new ArrayList<String>();

			Processor[] processors = model.getProcessorsOfType(GT4Processor.class);
			for (Processor processor : processors) {
				String loc = ((GT4Processor) processor).getWSDLLocation();
				if (!existingLocations.contains(loc)) {
					existingLocations.add(loc);
					try {
						result.add(new GT4Scavenger(loc,null));
					} catch (ScavengerCreationException e) {
						logger.warn("Error creating Biomoby Scavenger", e);
					}
				}
			}
			return result;
		}
		
		/**
		 * Returns the icon for this scavenger
		 */
		public ImageIcon getIcon() {
			return new GT4ProcessorInfoBean().icon();
		}
}


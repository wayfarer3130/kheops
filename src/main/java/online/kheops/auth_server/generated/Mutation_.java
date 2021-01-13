package online.kheops.auth_server.generated;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.Mutation;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.event.MutationType;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Mutation.class)
public abstract class Mutation_ extends online.kheops.auth_server.generated.Event_ {

	public static volatile SingularAttribute<Mutation, User> toUser;
	public static volatile SingularAttribute<Mutation, Capability> capability;
	public static volatile SingularAttribute<Mutation, MutationType> mutationType;
	public static volatile SingularAttribute<Mutation, ReportProvider> reportProvider;

	public static final String TO_USER = "toUser";
	public static final String CAPABILITY = "capability";
	public static final String MUTATION_TYPE = "mutationType";
	public static final String REPORT_PROVIDER = "reportProvider";

}


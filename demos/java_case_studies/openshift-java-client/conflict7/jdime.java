package com.openshift.internal.client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openshift.client.ApplicationScale;
import com.openshift.client.IApplication;
import com.openshift.client.IDomain;
import com.openshift.client.IGearProfile;
import com.openshift.client.IHttpClient;
import com.openshift.client.IUser;
import com.openshift.client.Messages;
import com.openshift.client.OpenShiftException;
import com.openshift.client.cartridge.IEmbeddableCartridge;
import com.openshift.client.cartridge.ICartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.internal.client.httpclient.JsonMediaType;
import com.openshift.internal.client.response.ApplicationResourceDTO;
import com.openshift.internal.client.response.DomainResourceDTO;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.LinkParameter;
import com.openshift.internal.client.utils.Assert;
import com.openshift.internal.client.utils.CollectionUtils;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

/**
 * @author André Dietisheim
 * @author Nicolas Spano
 */
public class DomainResource extends AbstractOpenShiftResource implements IDomain {
  private static final String LINK_GET = "GET";

  private static final String LINK_LIST_APPLICATIONS = "LIST_APPLICATIONS";

  private static final String LINK_ADD_APPLICATION = "ADD_APPLICATION";

  private static final String LINK_UPDATE = "UPDATE";

  private static final String LINK_DELETE = "DELETE";

  private static final String URL_REGEX = "(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

  private String id;

  private String suffix;

  private final APIResource connectionResource;

  /** Applications for the domain. */
  private List<IApplication> applications = null;

  protected DomainResource(final String namespace, final String suffix, final Map<String, Link> links, final Messages messages, final APIResource api) {
    super(api.getService(), links, messages);
    this.id = namespace;
    this.suffix = suffix;
    this.connectionResource = api;
  }

  protected DomainResource(DomainResourceDTO domainDTO, final APIResource api) {
    this(domainDTO.getId(), domainDTO.getSuffix(), domainDTO.getLinks(), domainDTO.getMessages(), api);
  }

  public String getId() {
    return id;
  }

  public String getSuffix() {
    return suffix;
  }

  public void rename(String id) throws OpenShiftException {
    Assert.notNull(id);
    DomainResourceDTO domainDTO = new UpdateDomainRequest().execute(id);
    this.id = domainDTO.getId();
    this.suffix = domainDTO.getSuffix();
    this.getLinks().clear();
    this.getLinks().putAll(domainDTO.getLinks());
  }

  public IUser getUser() throws OpenShiftException {
    return connectionResource.getUser();
  }

  public boolean waitForAccessible(long timeout) throws OpenShiftException {
    throw new UnsupportedOperationException();
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge) throws OpenShiftException {
    return createApplication(name, cartridge, (String) null);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final ApplicationScale scale) throws OpenShiftException {
    return createApplication(name, cartridge, scale, null, null);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, String initialGitUrl) throws OpenShiftException {
    return createApplication(name, cartridge, null, null, initialGitUrl);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final ApplicationScale scale, String initialGitUrl) throws OpenShiftException {
    return createApplication(name, cartridge, scale, null, initialGitUrl);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final IGearProfile gearProfile) throws OpenShiftException {
    return createApplication(name, cartridge, null, gearProfile);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final IGearProfile gearProfile, String initialGitUrl) throws OpenShiftException {
    return createApplication(name, cartridge, null, gearProfile, initialGitUrl);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final ApplicationScale scale, final IGearProfile gearProfile) throws OpenShiftException {
    return createApplication(name, cartridge, scale, gearProfile, null);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final ApplicationScale scale, final IGearProfile gearProfile, String initialGitUrl) throws OpenShiftException {
    return createApplication(name, cartridge, scale, gearProfile, initialGitUrl, IHttpClient.NO_TIMEOUT);
  }

  public IApplication createApplication(final String name, final IStandaloneCartridge cartridge, final ApplicationScale scale, final IGearProfile gearProfile, String initialGitUrl, int timeout, IEmbeddableCartridge... cartridges) throws OpenShiftException {
    if (name == null) {
      throw new OpenShiftException("Application name is mandatory but none was given.");
    }
    if (hasApplicationByName(name)) {
      throw new OpenShiftException("Application with name \"{0}\" already exists.", name);
    }
    ApplicationResourceDTO applicationDTO = new CreateApplicationRequest().execute(name, cartridge, scale, gearProfile, initialGitUrl, timeout, cartridges);
    IApplication application = new ApplicationResource(applicationDTO, cartridge, this);
    getOrLoadApplications().add(application);
    return application;
  }

  public boolean hasApplicationByName(String name) throws OpenShiftException {
    return getApplicationByName(name) != null;
  }

  public IApplication getApplicationByName(String name) throws OpenShiftException {
    Assert.notNull(name);
    return getApplicationByName(name, getApplications());
  }

  private IApplication getApplicationByName(String name, Collection<IApplication> applications) throws OpenShiftException {
    Assert.notNull(name);
    IApplication matchingApplication = null;
    for (IApplication application : applications) {
      if (application.getName().equalsIgnoreCase(name)) {
        matchingApplication = application;
        break;
      }
    }
    return matchingApplication;
  }

  public List<IApplication> getApplicationsByCartridge(IStandaloneCartridge cartridge) throws OpenShiftException {
    List<IApplication> matchingApplications = new ArrayList<IApplication>();
    for (IApplication application : getApplications()) {
      if (cartridge.equals(application.getCartridge())) {
        matchingApplications.add(application);
      }
    }
    return matchingApplications;
  }

  public boolean hasApplicationByCartridge(IStandaloneCartridge cartridge) throws OpenShiftException {
    return getApplicationsByCartridge(cartridge).size() > 0;
  }

  public void destroy() throws OpenShiftException {
    destroy(false);
  }

  public void destroy(boolean force) throws OpenShiftException {
    new DeleteDomainRequest().execute(force);
    connectionResource.removeDomain(this);
  }

  protected List<IApplication> getOrLoadApplications() throws OpenShiftException {
    if (applications == null) {
      this.applications = loadApplications();
    }
    return applications;
  }

  public List<IApplication> getApplications() throws OpenShiftException {
    return CollectionUtils.toUnmodifiableCopy(getOrLoadApplications());
  }

  /**
	 * @throws OpenShiftException
	 */
  private List<IApplication> loadApplications() throws OpenShiftException {
    List<IApplication> apps = new ArrayList<IApplication>();
    List<ApplicationResourceDTO> applicationDTOs = new ListApplicationsRequest().execute();
    for (ApplicationResourceDTO applicationDTO : applicationDTOs) {
      final IStandaloneCartridge cartridge = new StandaloneCartridge(applicationDTO.getFramework());
      final IApplication application = new ApplicationResource(applicationDTO, cartridge, this);
      apps.add(application);
    }
    return apps;
  }

  protected void removeApplication(IApplication application) {
    this.applications.remove(application);
  }

  public List<String> getAvailableCartridgeNames() throws OpenShiftException {
    final List<String> cartridges = new ArrayList<String>();
    for (LinkParameter param : getLink(LINK_ADD_APPLICATION).getRequiredParams()) {
      if (param.getName().equals("cartridge")) {
        for (String option : param.getValidOptions()) {
          cartridges.add(option);
        }
      }
    }
    return cartridges;
  }

  public List<IGearProfile> getAvailableGearProfiles() throws OpenShiftException {
    final List<IGearProfile> gearSizes = new ArrayList<IGearProfile>();
    for (LinkParameter param : getLink(LINK_ADD_APPLICATION).getOptionalParams()) {
      if (param.getName().equals(IOpenShiftJsonConstants.PROPERTY_GEAR_PROFILE)) {
        for (String option : param.getValidOptions()) {
          gearSizes.add(new GearProfile(option));
        }
      }
    }
    return gearSizes;
  }

  public void refresh() throws OpenShiftException {
    final DomainResourceDTO domainResourceDTO = new GetDomainRequest().execute();
    this.id = domainResourceDTO.getId();
    this.suffix = domainResourceDTO.getSuffix();
    if (this.applications != null) {
      this.applications = loadApplications();
    }
  }

  @Override public String toString() {
    return "Domain [" + "id=" + id + ", " + "suffix=" + suffix + "]";
  }

  private class GetDomainRequest extends ServiceRequest {
    public GetDomainRequest() throws OpenShiftException {
      super(LINK_GET);
    }

    protected DomainResourceDTO execute() throws OpenShiftException {
      return (DomainResourceDTO) super.execute();
    }
  }

  private class ListApplicationsRequest extends ServiceRequest {
    public ListApplicationsRequest() throws OpenShiftException {
      super(LINK_LIST_APPLICATIONS);
    }
  }

  private class CreateApplicationRequest extends ServiceRequest {
    public CreateApplicationRequest() throws OpenShiftException {
      super(LINK_ADD_APPLICATION);
    }

    public ApplicationResourceDTO execute(final String name, IStandaloneCartridge cartridge, final ApplicationScale scale, final IGearProfile gearProfile, final String initialGitUrl, final int timeout, final IEmbeddableCartridge... embeddableCartridges) throws OpenShiftException {
      if (cartridge == null) {
        throw new OpenShiftException("Application cartridge is mandatory but was not given.");
      }
      RequestParameters parameters = new RequestParameters().add(IOpenShiftJsonConstants.PROPERTY_NAME, name).addCartridges(cartridge, embeddableCartridges).addScale(scale).addGearProfile(gearProfile).add(IOpenShiftJsonConstants.PROPERTY_INITIAL_GIT_URL, initialGitUrl);

<<<<<<< left.java
      return super.execute(timeout, parameters.toArray());
=======
>>>>>>> Unknown file: This is a bug in JDime.

      if (isDownloadableCartridge(cartridge)) {
        return super.execute(timeout, new JsonMediaType(), (ServiceParameter[]) parameters.toArray(new ServiceParameter[parameters.size()]));
      } else {
        return super.execute(timeout, (ServiceParameter[]) parameters.toArray(new ServiceParameter[parameters.size()]));
      }
    }


<<<<<<< Unknown file: This is a bug in JDime.
=======
    private List<ServiceParameter> addCartridgeParameter(IStandaloneCartridge cartridge, List<ServiceParameter> parameters) {
      if (cartridge == null) {
        return parameters;
      }
      if (isDownloadableCartridge(cartridge)) {
        Map<String, String> props = new HashMap<String, String>();
        props.put(IOpenShiftJsonConstants.PROPERTY_URL, cartridge.getName());
        parameters.add(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGES, Arrays.asList(props)));
      } else {
        parameters.add(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_CARTRIDGE, cartridge.getName()));
      }
      return parameters;
    }
>>>>>>> right.java


    private boolean isDownloadableCartridge(ICartridge cartridge) {
      return cartridge.getName().matches(URL_REGEX);
    }
  }

  private class UpdateDomainRequest extends ServiceRequest {
    public UpdateDomainRequest() throws OpenShiftException {
      super(LINK_UPDATE);
    }

    public DomainResourceDTO execute(String namespace) throws OpenShiftException {
      return super.execute(new RequestParameter(IOpenShiftJsonConstants.PROPERTY_ID, namespace));
    }
  }

  private class DeleteDomainRequest extends ServiceRequest {
    public DeleteDomainRequest() throws OpenShiftException {
      super(LINK_DELETE);
    }

    public void execute(boolean force) throws OpenShiftException {
      super.execute(new RequestParameter(IOpenShiftJsonConstants.PROPERTY_FORCE, force));
    }
  }
}
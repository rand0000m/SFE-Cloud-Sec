package com.orange.cloudsec.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.groupbasedpolicy.endpoint.EndpointRpcRegistry;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.*;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.ServiceFunctions;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.base.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.base.SfDataPlaneLocatorBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.ServiceFunctionChains;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChain;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChainBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChainKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.service.function.chain.SfcServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.service.function.chain.SfcServiceFunctionBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.ovs.rev140701.*;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.ovs.rev140701.bridge.OvsBridgeBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.ovs.rev140701.options.OvsOptionsBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.ServiceFunctionForwarders;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarder.base.SffDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarder.base.SffDataPlaneLocatorBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarder.base.sff.data.plane.locator.DataPlaneLocatorBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarderBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarderKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.service.function.forwarder.ServiceFunctionDictionary;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.service.function.forwarder.ServiceFunctionDictionaryBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.service.function.forwarder.service.function.dictionary.SffSfDataPlaneLocatorBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.ServiceFunctionPaths;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPath;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPathBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPathKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sft.rev140701.ServiceFunctionTypes;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sft.rev140701.service.function.types.ServiceFunctionType;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sft.rev140701.service.function.types.ServiceFunctionTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sft.rev140701.service.function.types.ServiceFunctionTypeKey;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.VxlanGpe;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.data.plane.locator.locator.type.IpBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.RegisterEndpointInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.OfOverlayContextInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.OfOverlayContextInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.OfOverlayNodeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.OfOverlayNodeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.nodes.node.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.ofoverlay.rev140528.nodes.node.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.HasDirection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.action.refs.ActionRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.action.refs.ActionRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.subject.feature.instance.ParameterValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.subject.feature.instance.ParameterValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ForwardingContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.PolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.forwarding.context.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.Clause;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.ClauseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.SubjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.subject.Rule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.contract.subject.RuleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.endpoint.group.ConsumerNamedSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.endpoint.group.ConsumerNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.endpoint.group.ProviderNamedSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.endpoint.group.ProviderNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.subject.feature.instances.ActionInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.subject.feature.instances.ActionInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.subject.feature.instances.ClassifierInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.policy.subject.feature.instances.ClassifierInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.networks.Network;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.overlay.rev150105.TunnelTypeVxlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.overlay.rev150105.TunnelTypeVxlanGpe;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Created by mrousse on 11/04/17.
 */
public class CloudConfig<T extends DataObject> implements CloudSecService {
    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    protected ArrayList<T> data;
    protected DataBroker dataBroker;
    protected ArrayList<Network> netList;
    protected EndpointRpcRegistry epRR;
    public CloudConfig(){
    }

    public CloudConfig(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.epRR = new EndpointRpcRegistry(this.dataBroker);

        this.data = new ArrayList<T>();
        this.netList = new ArrayList<>();
    }

    public T create(T obj, InstanceIdentifier iid, DataObject sfcObj){
        if(data.contains(obj))
            return null;

        data.add(obj);
        pushToStore(sfcObj, iid, LogicalDatastoreType.CONFIGURATION);
        return obj;
    }

    public T update(T objBefore, T objAfter, InstanceIdentifier iid, DataObject sfcObj){
        int objId = data.indexOf(objBefore);
        //deleteFromStore(iid, LogicalDatastoreType.CONFIGURATION);
        pushToStore(sfcObj, iid, LogicalDatastoreType.CONFIGURATION);

        data.set(objId, objAfter);
        return objAfter;
    }

    public boolean delete(T obj, InstanceIdentifier iid){
        deleteFromStore(iid, LogicalDatastoreType.CONFIGURATION);
        data.remove(obj);
        return false;
    }

    public <U extends DataObject> DataObject read(InstanceIdentifier<U> iid){
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<U> dataObjectOptional = null;
            dataObjectOptional = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, iid).get();
            if(dataObjectOptional.isPresent()){
                U dataNode = dataObjectOptional.get();
                return dataNode;
                /*for(DataObject obj: data){
                    if(obj.equals(dataNode))
                        return obj;
                }*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void dataTreeChanged(DataTreeModification mod){
        InstanceIdentifier iid = mod.getRootPath().getRootIdentifier();
        T dataAfter = (T) mod.getRootNode().getDataAfter();
        T dataBefore = (T) mod.getRootNode().getDataBefore();
        Class targetType = mod.getRootPath().getRootIdentifier().getTargetType();

        DataObject toSFC = null;
        if(dataAfter != null)
            toSFC = convertToSFC(dataAfter, iid, targetType);
        switch (mod.getRootNode().getModificationType()) {
            case WRITE:
                if(dataBefore == null){
                    this.create(dataAfter, getIid(targetType, dataAfter), toSFC);
                }else{
                    this.update(dataBefore, dataAfter, getIid(targetType, dataAfter), toSFC);
                }
                break;
            case DELETE:
                this.delete(dataBefore, getIid(targetType, dataBefore));
                break;
            case SUBTREE_MODIFIED:
                break;
            default:
                break;
        }
    }

    private <U extends DataObject> void pushToStore(U object, InstanceIdentifier<U> objectIid, LogicalDatastoreType dsType){
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();

        transaction.put(dsType, objectIid, object, true);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();
        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
        }
    }

    private void deleteFromStore(InstanceIdentifier<T> object, LogicalDatastoreType dsType){
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(dsType, object);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = transaction.submit();

        try {
            commitFuture.checkedGet();
            LOG.warn("Transaction success : {}", commitFuture);
        } catch (Exception e) {
            LOG.error("Transaction failed with error {}", e.getMessage());
            transaction.cancel();
        }
    }

    private InstanceIdentifier getIid(Class objectType, DataObject obj){
        Class CloudSF = org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                cloud.sec.rev150105.service.function.registry.ServiceFunction.class;
        Class CloudSFF = org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder.class;

        if(objectType == CloudSF){
            SfName sfName = new SfName(((org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                    cloud.sec.rev150105.service.function.registry.ServiceFunction) obj).getName());
            InstanceIdentifier<ServiceFunction> iid =
                    InstanceIdentifier.create(ServiceFunctions.class)
                    .child(ServiceFunction.class, new ServiceFunctionKey(sfName));
            return iid;
        }
        if(objectType == CloudSFF){
            SffName sffName = new SffName(((org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                    cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder) obj).getName());
            InstanceIdentifier<ServiceFunctionForwarder> iid =
                    InstanceIdentifier.create(ServiceFunctionForwarders.class)
                            .child(ServiceFunctionForwarder.class, new ServiceFunctionForwarderKey(sffName));
            return iid;
        }
        return null;
    }

    /*
    Fonction pour créer un objet SF ou SFF pour la configuration SFC correspondant à l'objet créé dans le data store de Cloud Sec

    Pour une SF, il ne crée pas l'attribut "service-function:sf-data-plane-locator:service-function-forwarder" car son nom est inconnu
    Pour une SFF il ne crée pas les attributs "service-node" et "service-function-forwarder-ovs:ovs-bridge"
     */
    private DataObject convertToSFC(T obj, InstanceIdentifier<T> iid, Class targetType){
        Class CloudSF = org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                cloud.sec.rev150105.service.function.registry.ServiceFunction.class;
        Class CloudSFF = org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder.class;

        if(targetType == CloudSF){
            // Si l'objet est une ServiceFunction
            ServiceFunctionBuilder builder = new ServiceFunctionBuilder();
            SfDataPlaneLocatorBuilder sfDPBuilder = new SfDataPlaneLocatorBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                    cloud.sec.rev150105.service.function.registry.ServiceFunction cloudSF =
                    (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                            cloud.sec.rev150105.service.function.registry.ServiceFunction) obj;

            // Création du DataPlaneLocator
            sfDPBuilder.setName(new SfDataPlaneLocatorName(cloudSF.getName()))
                    .setLocatorType(
                            new IpBuilder()
                                    .setIp(new IpAddress(cloudSF.getAddress()))
                                    .setPort(new PortNumber(6633))
                                    .build()
                    )
                    .setServiceFunctionForwarder(new SffName(""))
                    .setTransport(VxlanGpe.class);
            ArrayList<SfDataPlaneLocator> sfDPList = new ArrayList();
            sfDPList.add(sfDPBuilder.build());

            // Création du SF pour SFC
            builder.setName(new SfName(cloudSF.getName()))
                    .setNshAware(true)
                    .setType(new SftTypeName("service-function-type:".concat(cloudSF.getName())))
                    .setIpMgmtAddress(new IpAddress(cloudSF.getAddress()))
                    .setSfDataPlaneLocator(sfDPList);
            ServiceFunction output = builder.build();

            ServiceFunctionTypeBuilder serviceFunctionTypeBuilder = new ServiceFunctionTypeBuilder();
            serviceFunctionTypeBuilder.setNshAware(true)
                    .setBidirectionality(true)
                    .setSymmetry(true)
                    .setType(new SftTypeName(cloudSF.getName()));
            pushToStore(serviceFunctionTypeBuilder.build(), InstanceIdentifier.create(ServiceFunctionTypes.class)
                    .child(ServiceFunctionType.class, new ServiceFunctionTypeKey(
                            new SftTypeName(cloudSF.getName()))),
                    LogicalDatastoreType.CONFIGURATION);

            //LOG.warn("Looks like your Service Function : {}", output);
            return output;
        }else if(targetType == CloudSFF){
            // Si l'objet est une ServiceFunctionForwarder

            ServiceFunctionForwarderBuilder builder = new ServiceFunctionForwarderBuilder();
            SffDataPlaneLocatorBuilder sffDPBuilder = new SffDataPlaneLocatorBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                    cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder cloudSFF =
                    (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.
                            cloud.sec.rev150105.service.function.forwarder.registry.ServiceFunctionForwarder) obj;
            SffOvsLocatorOptionsAugmentationBuilder ovsOptionsAugBuilder =
                    new SffOvsLocatorOptionsAugmentationBuilder();
            SffOvsBridgeAugmentationBuilder ovsBridgeAugmentationBuilder =
                    new SffOvsBridgeAugmentationBuilder();
            OvsBridgeBuilder ovsBridgeBuilder = new OvsBridgeBuilder();
            IpBuilder ipBuilder = new IpBuilder();
            OvsOptionsBuilder ovsOptionsBuilder = new OvsOptionsBuilder();
            DataPlaneLocatorBuilder dpBuilder = new DataPlaneLocatorBuilder();
            ArrayList<SffDataPlaneLocator> dpList = new ArrayList<>();
            ArrayList<ServiceFunctionDictionary> sfList = new ArrayList<>();

            // Mise à jour de la SF
            InstanceIdentifier<ServiceFunction> sfIid = null;

            ipBuilder.setIp(new IpAddress(cloudSFF.getAddress()))
                    .setPort(new PortNumber(6633));
            dpBuilder.setLocatorType(ipBuilder.build())
                    .setTransport(VxlanGpe.class);
            ovsOptionsBuilder.setExts("gpe")
                    .setRemoteIp("flow")
                    .setDstPort("6633")
                    .setKey("flow")
                    .setNsp("flow")
                    .setNsi("flow")
                    .setNshc1("flow")
                    .setNshc2("flow")
                    .setNshc3("flow")
                    .setNshc4("flow");
            ovsOptionsAugBuilder.setOvsOptions(ovsOptionsBuilder.build());
            sffDPBuilder.setName(new SffDataPlaneLocatorName(cloudSFF.getName()))
                    .setDataPlaneLocator(dpBuilder.build())
                    .addAugmentation(SffOvsLocatorOptionsAugmentation.class, ovsOptionsAugBuilder.build());

            dpList.add(sffDPBuilder.build());

            ServiceFunctionDictionaryBuilder sfdBuilder = new ServiceFunctionDictionaryBuilder();
            SffSfDataPlaneLocatorBuilder sffSfDplBuilder = new SffSfDataPlaneLocatorBuilder();
            for(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cloud.sec.rev150105.service.function
                    .forwarder.registry.service.function.forwarder.ServiceFunctions sf: cloudSFF.getServiceFunctions()){
                sffSfDplBuilder.setSfDplName(new SfDataPlaneLocatorName(sf.getSfName()))
                        .setSffDplName(new SffDataPlaneLocatorName(cloudSFF.getName()));
                sfdBuilder.setName(new SfName(sf.getSfName()))
                        .setSffSfDataPlaneLocator(sffSfDplBuilder.build());
                sfList.add(sfdBuilder.build());

                // Mise à jour de la SF
                /*sfIid = InstanceIdentifier.create(ServiceFunctions.class)
                    .child(ServiceFunction.class, new ServiceFunctionKey(new SfName(sf.getSfName())));*/

                SfName sfName = new SfName(sf.getSfName());
                sfIid = InstanceIdentifier.create(ServiceFunctions.class).child(ServiceFunction.class, new ServiceFunctionKey(sfName));

                // Attention, risque de "Race condition" : si jamais la SF n'a pas été créé, bug à l'ajout de la SFF dans SFC
                DataObject dbSF = read(sfIid);
                ServiceFunctionBuilder sfBuilder = new ServiceFunctionBuilder();
                if(dbSF != null)
                    sfBuilder.fieldsFrom(dbSF);

                ArrayList<SfDataPlaneLocator> sfDataPlaneLocators = new ArrayList(sfBuilder.getSfDataPlaneLocator());
                for(int i = 0; i < sfDataPlaneLocators.size(); i++){
                    SfDataPlaneLocator sfDataPlaneLocator = sfDataPlaneLocators.get(i);
                    SfDataPlaneLocatorBuilder sfDataPlaneLocatorBuilder =
                            new SfDataPlaneLocatorBuilder(sfDataPlaneLocator);
                    sfDataPlaneLocatorBuilder.setServiceFunctionForwarder(new SffName(cloudSFF.getName()));
                    sfDataPlaneLocators.set(i, sfDataPlaneLocatorBuilder.build());
                }
                sfBuilder.setSfDataPlaneLocator(sfDataPlaneLocators);
                this.pushToStore(sfBuilder.build(), sfIid, LogicalDatastoreType.CONFIGURATION);
            }

            ovsBridgeBuilder.setBridgeName(cloudSFF.getOvsBridge());
            ovsBridgeAugmentationBuilder.setOvsBridge(ovsBridgeBuilder.build());

            // J'avais mis l'IpMgmtAddress en pensant qu'il remplaçait le Service Node (qui n'est pas initialisé)
            // J'ai retiré l'IpMgmtAddress car il n'est pas supposé être utilisé avec les SFF
            builder.setName(new SffName(cloudSFF.getName()))
                    //.setIpMgmtAddress(cloudSFF.getAddress())
                    .setSffDataPlaneLocator(dpList)
                    .setServiceFunctionDictionary(sfList)
                    .addAugmentation(SffOvsBridgeAugmentation.class, ovsBridgeAugmentationBuilder.build());
            ServiceFunctionForwarder output = builder.build();
            //LOG.warn("Looks like your Service Function Forwarder : {}", output);
            return output;
        }
        return null;
    }

    private void tenantCreate(){
        TenantBuilder tenantBuilder = new TenantBuilder();
        /* Forwarding Ctx */
        ForwardingContextBuilder forwardingContextBuilder = new ForwardingContextBuilder();
        InstanceIdentifier<Tenant> tenantIid = InstanceIdentifier.create(Tenants.class)
                .child(Tenant.class, new TenantKey(new TenantId("cloud-sec-tenant")));
        L3ContextBuilder l3ContextBuilder = new L3ContextBuilder();
        L3ContextId l3ContextId = new L3ContextId("l3-context-vrf");
        ArrayList<L3Context> l3Contexts = new ArrayList<>();
        L2FloodDomainBuilder l2FloodDomainBuilder = new L2FloodDomainBuilder();
        L2FloodDomainId floodDomain1 = new L2FloodDomainId("flood-domain-1");
        L2FloodDomainId floodDomain2 = new L2FloodDomainId("flood-domain-2");
        ArrayList<L2FloodDomain> l2FloodDomains = new ArrayList<>();
        L2BridgeDomainBuilder l2BridgeDomainBuilder = new L2BridgeDomainBuilder();
        L2BridgeDomainId l2BridgeDomainId = new L2BridgeDomainId("bridge-domain");
        ArrayList<L2BridgeDomain> l2BridgeDomains = new ArrayList<>();
        SubnetBuilder subnetBuilder = new SubnetBuilder();
        ArrayList<Subnet> subnets = new ArrayList<>();
        /* Policy */
        PolicyBuilder policyBuilder = new PolicyBuilder();
        EndpointGroupBuilder endpointGroupBuilder = new EndpointGroupBuilder();
        ArrayList<EndpointGroup> endpoints = new ArrayList<>();
        ProviderNamedSelectorBuilder providerNamedSelectorBuilder = new ProviderNamedSelectorBuilder();
        ArrayList<ProviderNamedSelector> providerNamedSelectors = new ArrayList<>();
        ConsumerNamedSelectorBuilder consumerNamedSelectorBuilder = new ConsumerNamedSelectorBuilder();
        ArrayList<ConsumerNamedSelector> consumerNamedSelectors = new ArrayList<>();
        SubjectFeatureInstancesBuilder subjectFeatureInstancesBuilder = new SubjectFeatureInstancesBuilder();
        ClassifierInstanceBuilder classifierInstanceBuilder = new ClassifierInstanceBuilder();
        ArrayList<ClassifierInstance> classifierInstances = new ArrayList<>();
        ParameterValueBuilder parameterValueBuilder = new ParameterValueBuilder();
        ArrayList<ParameterValue> parameterValuesActionChain = new ArrayList<>();
        ActionInstanceBuilder actionInstanceBuilder = new ActionInstanceBuilder();
        ArrayList<ActionInstance> actionInstances = new ArrayList<>();
        ContractBuilder contractBuilder = new ContractBuilder();
        ArrayList<Contract> contracts = new ArrayList<>();
        ArrayList<ContractId> contractIds = new ArrayList<>();
        SubjectBuilder subjectBuilder = new SubjectBuilder();
        ArrayList<Subject> subjects = new ArrayList<>();
        ArrayList<SubjectName> subjectRefs = new ArrayList<>();
        RuleBuilder ruleBuilder = new RuleBuilder();
        ArrayList<Rule> rules = new ArrayList<>();
        ClassifierRefBuilder classifierRefBuilder = new ClassifierRefBuilder();
        ArrayList<ClassifierRef> classifierRefs = new ArrayList<>();
        ActionRefBuilder actionRefBuilder = new ActionRefBuilder();
        ArrayList<ActionRef> actionRefs = new ArrayList<>();
        ClauseBuilder clauseBuilder = new ClauseBuilder();
        ArrayList<Clause> clauses = new ArrayList<>();

        /* FORWARDING CONTEXT */
        l3ContextBuilder.setId(l3ContextId);
        l3Contexts.add(l3ContextBuilder.build());

        l2BridgeDomainBuilder.setId(l2BridgeDomainId)
                .setParent(l3ContextId);
        l2BridgeDomains.add(l2BridgeDomainBuilder.build());

        l2FloodDomainBuilder.setId(floodDomain1)
                .setParent(l2BridgeDomainId);
        l2FloodDomains.add(l2FloodDomainBuilder.build());
        l2FloodDomainBuilder.setId(floodDomain2);
        l2FloodDomains.add(l2FloodDomainBuilder.build());

        subnetBuilder.setId(new SubnetId("subnet-10.0.36.0/24"))
                .setVirtualRouterIp(new IpAddress("10.0.36.1".toCharArray()))
                .setParent(floodDomain1)
                .setIpPrefix(new IpPrefix("10.0.36.1/24".toCharArray()));
        subnets.add(subnetBuilder.build());
        subnetBuilder.setId(new SubnetId("subnet-10.0.35.0/24"))
                .setVirtualRouterIp(new IpAddress("10.0.35.1".toCharArray()))
                .setParent(floodDomain2)
                .setIpPrefix(new IpPrefix("10.0.35.1/24".toCharArray()));
        subnets.add(subnetBuilder.build());

        forwardingContextBuilder.setL3Context(l3Contexts)
                .setL2BridgeDomain(l2BridgeDomains)
                .setL2FloodDomain(l2FloodDomains)
                .setSubnet(subnets);

        /* POLICY */
        ContractId contractId = new ContractId("icmp-http-contract");
        contractIds.add(contractId);

        SelectorName selectorName = new SelectorName("webservers-clients-icmp-http-contract");

        providerNamedSelectorBuilder.setName(selectorName)
                .setContract(contractIds);
        providerNamedSelectors.add(providerNamedSelectorBuilder.build());
        endpointGroupBuilder.setId(new EndpointGroupId("webservers"))
                .setName(new Name("webservers"))
                .setProviderNamedSelector(providerNamedSelectors);
        endpoints.add(endpointGroupBuilder.build());

        consumerNamedSelectorBuilder.setName(selectorName)
                .setContract(contractIds);
        consumerNamedSelectors.add(consumerNamedSelectorBuilder.build());
        endpointGroupBuilder.setId(new EndpointGroupId("clients"))
                .setName(new Name("clients"))
                .setConsumerNamedSelector(consumerNamedSelectors);
        endpoints.add(endpointGroupBuilder.build());

        parameterValueBuilder.setName(new ParameterName("proto"))
                .setIntValue(new Long(1));
        ArrayList<ParameterValue> parameterValuesIcmp = new ArrayList<>();
        parameterValuesIcmp.add(parameterValueBuilder.build());
        classifierInstanceBuilder.setClassifierDefinitionId(new ClassifierDefinitionId("Classifier-IP-Protocol"))
                .setName(new ClassifierName("icmp"))
                .setParameterValue(parameterValuesIcmp);
        classifierInstances.add(classifierInstanceBuilder.build());
        parameterValueBuilder.setName(new ParameterName("proto"))
                .setIntValue(new Long(6));
        ArrayList<ParameterValue> parameterValuesHttpDst = new ArrayList<>();
        parameterValuesHttpDst.add(parameterValueBuilder.build());
        parameterValueBuilder.setName(new ParameterName("destport"))
                .setIntValue(new Long(80));
        parameterValuesHttpDst.add(parameterValueBuilder.build());
        classifierInstanceBuilder.setClassifierDefinitionId(new ClassifierDefinitionId("Classifier-L4"))
                .setName(new ClassifierName("http-dest"))
                .setParameterValue(parameterValuesHttpDst);
        classifierInstances.add(classifierInstanceBuilder.build());
        ArrayList<ParameterValue> parameterValuesHttpSrc = new ArrayList<>();
        parameterValueBuilder.setName(new ParameterName("proto"))
                .setIntValue(new Long(6));
        parameterValuesHttpSrc.add(parameterValueBuilder.build());
        parameterValueBuilder.setName(new ParameterName("sourceport"))
                .setIntValue(new Long(80));
        parameterValuesHttpSrc.add(parameterValueBuilder.build());
        classifierInstanceBuilder.setClassifierDefinitionId(new ClassifierDefinitionId("Classifier-L4"))
                .setName(new ClassifierName("http-src"))
                .setParameterValue(parameterValuesHttpSrc);
        classifierInstances.add(classifierInstanceBuilder.build());

        parameterValueBuilder = new ParameterValueBuilder();
        actionInstanceBuilder.setName(new ActionName("allow1"))
                .setActionDefinitionId(new ActionDefinitionId("Action-Allow"));
        actionInstances.add(actionInstanceBuilder.build());
        parameterValueBuilder.setName(new ParameterName("sfc-chain-name"))
                .setStringValue("SFCGBP");
        parameterValuesActionChain.add(parameterValueBuilder.build());
        actionInstanceBuilder.setName(new ActionName("chain1"))
                .setActionDefinitionId(new ActionDefinitionId("Action-Chain"))
                .setParameterValue(parameterValuesActionChain);
        actionInstances.add(actionInstanceBuilder.build());

        subjectFeatureInstancesBuilder.setActionInstance(actionInstances)
                .setClassifierInstance(classifierInstances);

        classifierRefBuilder.setName(new ClassifierName("icmp-in"))
                .setInstanceName(new ClassifierName("icmp"))
                .setDirection(HasDirection.Direction.In);
        classifierRefs.add(classifierRefBuilder.build());
        classifierRefBuilder.setName(new ClassifierName("icmp-in"))
                .setInstanceName(new ClassifierName("icmp"))
                .setDirection(HasDirection.Direction.In);
        classifierRefs.add(classifierRefBuilder.build());

        actionRefBuilder.setName(new ActionName("allow1"))
                .setOrder(0);
        actionRefs.add(actionRefBuilder.build());

        ruleBuilder.setName(new RuleName("allow-icmp-rule"))
                .setOrder(0)
                .setClassifierRef(classifierRefs)
                .setActionRef(actionRefs);
        rules.add(ruleBuilder.build());

        subjectBuilder.setName(new SubjectName("icmp-subject"))
                .setRule(rules);
        subjects.add(subjectBuilder.build());

        classifierRefs.clear();
        classifierRefBuilder.setName(new ClassifierName("http-dest"))
                .setInstanceName(new ClassifierName("http-dest"))
                .setDirection(HasDirection.Direction.In);
        classifierRefs.add(classifierRefBuilder.build());

        actionRefs.clear();
        actionRefBuilder.setName(new ActionName("chain1"))
                .setOrder(0);
        actionRefs.add(actionRefBuilder.build());

        rules.clear();
        ruleBuilder.setName(new RuleName("http-chain-rule-in"))
                .setActionRef(actionRefs)
                .setClassifierRef(classifierRefs);
        rules.add(ruleBuilder.build());

        classifierRefs.clear();
        actionRefs.clear();
        classifierRefBuilder.setName(new ClassifierName("http-src"))
                .setInstanceName(new ClassifierName("http-src"))
                .setDirection(HasDirection.Direction.Out);
        classifierRefs.add(classifierRefBuilder.build());

        actionRefBuilder.setName(new ActionName("chain1"))
                .setOrder(0);
        actionRefs.add(actionRefBuilder.build());

        ruleBuilder.setName(new RuleName("http-chain-rule-out"))
                .setClassifierRef(classifierRefs)
                .setActionRef(actionRefs);
        rules.add(ruleBuilder.build());

        subjectBuilder.setName(new SubjectName("http-subject"))
                .setRule(rules);
        subjects.add(subjectBuilder.build());

        subjectRefs.add(new SubjectName("icmp-subject"));
        subjectRefs.add(new SubjectName("http-subject"));

        clauseBuilder.setName(new ClauseName("icmp-http-clause"))
                .setSubjectRefs(subjectRefs);
        clauses.add(clauseBuilder.build());

        contractBuilder.setId(new ContractId("icmp-http-contract"))
                .setSubject(subjects)
                .setClause(clauses);
        contracts.add(contractBuilder.build());

        policyBuilder.setEndpointGroup(endpoints)
                .setSubjectFeatureInstances(subjectFeatureInstancesBuilder.build())
                .setContract(contracts);

        /* TENANT */
        tenantBuilder.setId(new TenantId("cloud-sec-tenant"))
                .setName(new Name("CloudSecTenant"))
                .setForwardingContext(forwardingContextBuilder.build())
                .setPolicy(policyBuilder.build());

        pushToStore(tenantBuilder.build(), tenantIid, LogicalDatastoreType.CONFIGURATION);
    }

    public void endpointCreate(String endpointGroup,
                                String networkContainment,
                                String macAddr,
                                String l3Addr,
                                String portName){
        RegisterEndpointInputBuilder registerEndpointInputBuilder = new RegisterEndpointInputBuilder();
        L3AddressBuilder l3AddressBuilder = new L3AddressBuilder();
        ArrayList<L3Address> l3Addresses = new ArrayList<>();
        OfOverlayContextInputBuilder ofOverlayContextInputBuilder = new OfOverlayContextInputBuilder();

        ofOverlayContextInputBuilder.setPortName(new Name(portName));

        l3AddressBuilder.setIpAddress(new IpAddress(l3Addr.toCharArray()))
                .setL3Context(new L3ContextId("l3-context-vrf"));
        l3Addresses.add(l3AddressBuilder.build());

        registerEndpointInputBuilder.setEndpointGroup(new EndpointGroupId(endpointGroup))
                .setNetworkContainment(new NetworkDomainId(networkContainment))
                .setL2Context(new L2BridgeDomainId("bridge-domain"))
                .setMacAddress(new MacAddress(macAddr))
                .setL3Address(l3Addresses)
                .addAugmentation(OfOverlayContextInput.class, ofOverlayContextInputBuilder.build())
                .setTenant(new TenantId("cloud-sec-tenant"));

        // PAS DE PORT-NAME DE TROUVÉ DANS LES YANG OU LE PROJET GROUPBASEDPOLICY

        epRR.registerEndpoint(registerEndpointInputBuilder.build());
    }

    public void tunnelCreate(String ipAddr,
                             String tunId){
        NodeBuilder nodeBuilder = new NodeBuilder();
        OfOverlayNodeConfigBuilder ofOverlayNodeConfigBuilder = new OfOverlayNodeConfigBuilder();
        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        ArrayList<Tunnel> tunnels = new ArrayList<>();
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> nodeIid =
                InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(tunId)));

        tunnelBuilder.setIp(new IpAddress(ipAddr.toCharArray()))
                .setTunnelType(TunnelTypeVxlanGpe.class)
                .setNodeConnectorId(new NodeConnectorId(tunId.concat(":1")))
                .setPort(new PortNumber(6633));
        tunnels.add(tunnelBuilder.build());

        tunnelBuilder.setIp(new IpAddress(ipAddr.toCharArray()))
                .setTunnelType(TunnelTypeVxlan.class)
                .setNodeConnectorId(new NodeConnectorId(tunId.concat(":2")))
                .setPort(new PortNumber(4789));
        tunnels.add(tunnelBuilder.build());

        ofOverlayNodeConfigBuilder.setTunnel(tunnels);
        nodeBuilder.addAugmentation(OfOverlayNodeConfig.class, ofOverlayNodeConfigBuilder.build())
                .setId(new NodeId(tunId));

        pushToStore(nodeBuilder.build(), nodeIid, LogicalDatastoreType.CONFIGURATION);
    }

    public void functionPathCreate(String name,
                                   ArrayList<String> functions)
    {
        ServiceFunctionChainBuilder serviceFunctionChainBuilder = new ServiceFunctionChainBuilder();
        SfcServiceFunctionBuilder sfcServiceFunctionBuilder = new SfcServiceFunctionBuilder();
        ArrayList<SfcServiceFunction> sfcServiceFunctions = new ArrayList<>();

        for(int i = 0; i < functions.size(); i++){
            sfcServiceFunctionBuilder.setName(functions.get(i))
                    .setType(new SftTypeName("service-function-type:".concat(functions.get(i))));
            sfcServiceFunctions.add(sfcServiceFunctionBuilder.build());
        }
        serviceFunctionChainBuilder.setName(new SfcName(name))
                .setSymmetric(true)
                .setSfcServiceFunction(sfcServiceFunctions);

        pushToStore(serviceFunctionChainBuilder.build(),
                InstanceIdentifier.create(ServiceFunctionChains.class)
                        .child(ServiceFunctionChain.class, new ServiceFunctionChainKey(new SfcName(name))),
                LogicalDatastoreType.CONFIGURATION);

        ServiceFunctionPathBuilder serviceFunctionPathBuilder = new ServiceFunctionPathBuilder();
        serviceFunctionPathBuilder.setName(new SfpName(name.concat("-Path")))
                .setServiceChainName(new SfcName(name))
                .setStartingIndex((short) 255)
                .setSymmetric(true);

        pushToStore(serviceFunctionPathBuilder.build(),
                InstanceIdentifier.create(ServiceFunctionPaths.class)
                    .child(ServiceFunctionPath.class, new ServiceFunctionPathKey(new SfpName(name.concat("-Path")))),
                LogicalDatastoreType.CONFIGURATION);
    }

    @Override
    public Future<RpcResult<CreateTenantOutput>> createTenant(CreateTenantInput input) {
        CreateTenantOutputBuilder createTenantOutputBuilder = new CreateTenantOutputBuilder();
        createTenantOutputBuilder.setUnused(input.getUnused());
        this.tenantCreate();
        return RpcResultBuilder.success(createTenantOutputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateFunctionPathOutput>> createFunctionPath(CreateFunctionPathInput input) {
        ArrayList<String> functions = new ArrayList<>();
        CreateFunctionPathOutputBuilder createFunctionPathOutputBuilder = new CreateFunctionPathOutputBuilder();
        createFunctionPathOutputBuilder.setUnused(input.getUnused());
        functions.add("dpi");
        functions.add("firewall");

        functionPathCreate("SFCGBP", functions);
        return RpcResultBuilder.success(createFunctionPathOutputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateTunnelOutput>> createTunnel(CreateTunnelInput input) {
        CreateTunnelOutputBuilder createTunnelOutputBuilder = new CreateTunnelOutputBuilder();
        createTunnelOutputBuilder.setUnused(input.getUnused());
        this.tunnelCreate("192.168.50.75", "openflow:6");
        this.tunnelCreate("192.168.50.70", "openflow:1");
        return RpcResultBuilder.success(createTunnelOutputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<CreateEndpointsOutput>> createEndpoints(CreateEndpointsInput input) {
        CreateEndpointsOutputBuilder createEndpointsOutputBuilder = new CreateEndpointsOutputBuilder();
        createEndpointsOutputBuilder.setUnused(input.getUnused());
        this.endpointCreate("webservers",
                "subnet-10.0.36.0/24",
                "00:00:00:00:36:02",
                "10.0.36.2",
                "vethl-h36-2");
        this.endpointCreate("clients",
                "subnet-10.0.35.0/24",
                "00:00:00:00:35:02",
                "10.0.35.2",
                "vethl-h35-2");
        this.endpointCreate("clients",
                "subnet-10.0.35.0/24",
                "00:00:00:00:35:03",
                "10.0.35.3",
                "vethl-h35-3");
        this.endpointCreate("webservers",
                "subnet-10.0.36.0/24",
                "00:00:00:00:36:03",
                "10.0.36.3",
                "vethl-h36-3");
        this.endpointCreate("clients",
                "subnet-10.0.35.0/24",
                "00:00:00:00:35:04",
                "10.0.35.4",
                "vethl-h35-4");
        this.endpointCreate("webservers",
                "subnet-10.0.36.0/24",
                "00:00:00:00:36:04",
                "10.0.36.4",
                "vethl-h36-4");
        this.endpointCreate("clients",
                "subnet-10.0.35.0/24",
                "00:00:00:00:35:05",
                "10.0.35.5",
                "vethl-h35-5");
        this.endpointCreate("webservers",
                "subnet-10.0.36.0/24",
                "00:00:00:00:36:05",
                "10.0.36.5",
                "vethl-h36-5");
        return RpcResultBuilder.success(createEndpointsOutputBuilder.build()).buildFuture();
    }
}

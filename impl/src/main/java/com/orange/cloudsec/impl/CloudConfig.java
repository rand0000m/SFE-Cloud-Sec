package com.orange.cloudsec.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.*;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.ServiceFunctions;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.base.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.base.SfDataPlaneLocatorBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunctionKey;
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
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.SlTransportType;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.VxlanGpe;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sl.rev140701.data.plane.locator.locator.type.IpBuilder;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sf.map.rev140701.service.function.mapping.SlTransportsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by mrousse on 11/04/17.
 */
public class CloudConfig<T extends DataObject> {
    private static final Logger LOG = LoggerFactory.getLogger(CloudSecProvider.class);

    protected ArrayList<T> data;
    protected DataBroker dataBroker;

    public CloudConfig(){
    }

    public CloudConfig(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

        this.data = new ArrayList<T>();
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

    public DataObject read(InstanceIdentifier<T> iid){
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> dataObjectOptional = null;
            dataObjectOptional = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, iid).get();
            if(dataObjectOptional.isPresent()){
                T dataNode = dataObjectOptional.get();
                for(DataObject obj: data){
                    if(obj.equals(dataNode))
                        return obj;
                }
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
                this.delete(dataBefore, getIid(targetType, dataAfter));
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
                    .setType(new SftTypeName("service-function-type:dpi"))
                    .setIpMgmtAddress(new IpAddress(cloudSF.getAddress()))
                    .setSfDataPlaneLocator(sfDPList);
            ServiceFunction output = builder.build();

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
}

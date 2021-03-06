/*
 * Copyright (C) 2013, Alliance for Sustainable Energy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 package gov.nrel.bacnet.consumer;

import gov.nrel.bacnet.consumer.beans.Device;
// import gov.nrel.bacnet.consumer.beans.JsonAllFilters;
import gov.nrel.bacnet.consumer.beans.Stream;
import gov.nrel.bacnet.consumer.beans.ObjKey;


import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import com.google.gson.Gson;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.util.PropertyValues;

class PropertyLoader  {
// TODO fix logger
  private static final Logger log = Logger.getLogger(PropertiesReader.class.getName());
  private LocalDevice localDevice;
  
  public PropertyLoader(LocalDevice ld) {
    localDevice = ld;
  }

  public List<ObjectIdentifier> getOids(RemoteDevice rd) throws BACnetException{
    Map<ObjKey, Encodable> properties;
    List<ObjectIdentifier> allOids = ((SequenceOf<ObjectIdentifier>) localDevice
          .sendReadPropertyAllowNull(rd,
              rd.getObjectIdentifier(),
              PropertyIdentifier.objectList)).getValues();

    return allOids;
  }

// possible that this must be run after getOids as that runs "getExtendedInformation"
  public Map<ObjKey, String> getProperties(RemoteDevice rd, List<ObjectIdentifier> oids) throws BACnetException {
    Map<ObjKey, String> properties = new HashMap<ObjKey, String>();
    PropertyReferences refs = setupRefs(oids);

    //System.out.println("oids size = "+oids.size()+" refs size = "+ refs.size());
    PropertyValues propVals = localDevice.readProperties(rd, refs);
    //System.out.println("properties read");
    // //Here we have an iterator of units and objectNames....
    Iterator<ObjectPropertyReference> iterator = propVals.iterator();
    while(iterator.hasNext()) {
      ObjectPropertyReference ref = iterator.next();
      ObjectIdentifier oid = ref.getObjectIdentifier();
      PropertyIdentifier id = ref.getPropertyIdentifier();
      
      try {
        // get encodable from propertyvalues by objectpropertyreference and convert  to string
        String value = propVals.get(ref) + "";
        ObjKey k = new ObjKey(oid, id);
        properties.put(k, value);
      } catch(Exception e) {
        log.info("Exception reading prop oid="+oid+" from id="+id+" device="+rd);
        //Tons of stuff has no units and some stuff has no objectNames
      }
    }
    return properties;
  }
  // filter was applied here in original code
  // basically, we are requesting 2 prop values from each oid on the device:
  // units and objectName
  private PropertyReferences setupRefs(List<ObjectIdentifier> cachedOids) {
    PropertyReferences refs = new PropertyReferences();
    for(ObjectIdentifier oid : cachedOids) {
        refs.add(oid, PropertyIdentifier.units);
        refs.add(oid, PropertyIdentifier.objectName);
        // refs.add(oid, PropertyIdentifier.presentValue);
    }
    // this should return refs instead.  oidstopoll aren't needed now
    return refs;
  }




}

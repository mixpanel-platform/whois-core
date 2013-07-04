//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.04 at 09:28:58 AM EST 
//


package net.ripe.db.whois.api.whois.rdap.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{}rdapObject">
 *       &lt;sequence>
 *         &lt;element name="handle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ldhName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="unicodeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nameservers" type="{}nameserver" maxOccurs="unbounded"/>
 *         &lt;element name="publicIds" type="{}hashMapType" minOccurs="0"/>
 *         &lt;element name="port43" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "nameservers",
    "publicIds",
    "port43"
})
@XmlRootElement(name = "domain")
public class Domain
    extends RdapObject
    implements Serializable
{

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String ldhName;
    @XmlElement(required = true)
    protected String unicodeName;
    @XmlElement(required = true)
    protected List<Nameserver> nameservers;
    protected HashMap publicIds;
    protected String port43;

    /**
     * Gets the value of the handle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the value of the handle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandle(String value) {
        this.handle = value;
    }

    /**
     * Gets the value of the ldhName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLdhName() {
        return ldhName;
    }

    /**
     * Sets the value of the ldhName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLdhName(String value) {
        this.ldhName = value;
    }

    /**
     * Gets the value of the unicodeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnicodeName() {
        return unicodeName;
    }

    /**
     * Sets the value of the unicodeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnicodeName(String value) {
        this.unicodeName = value;
    }

    /**
     * Gets the value of the nameservers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameservers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNameservers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Nameserver }
     * 
     * 
     */
    public List<Nameserver> getNameservers() {
        if (nameservers == null) {
            nameservers = new ArrayList<Nameserver>();
        }
        return this.nameservers;
    }

    /**
     * Gets the value of the publicIds property.
     * 
     * @return
     *     possible object is
     *     {@link HashMap }
     *     
     */
    public HashMap getPublicIds() {
        return publicIds;
    }

    /**
     * Sets the value of the publicIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link HashMap }
     *     
     */
    public void setPublicIds(HashMap value) {
        this.publicIds = value;
    }

    /**
     * Gets the value of the port43 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPort43() {
        return port43;
    }

    /**
     * Sets the value of the port43 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPort43(String value) {
        this.port43 = value;
    }

}
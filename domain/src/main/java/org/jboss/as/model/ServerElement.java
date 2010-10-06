/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.model;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.model.socket.ServerInterfaceElement;
import org.jboss.as.model.socket.SocketBindingGroupRefElement;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * An individual server on a {@link HostModel}.
 *
 * @author Brian Stansberry
 */
public final class ServerElement extends AbstractModelElement<ServerElement> {

    private static final long serialVersionUID = 7667892965813702351L;

    private final String name;
    private final String serverGroup;
    private final NavigableMap<String, ServerInterfaceElement> interfaces = new TreeMap<String, ServerInterfaceElement>();
    private boolean start;
    private SocketBindingGroupRefElement bindingGroup;
    private JvmElement jvm;
    private PropertiesElement systemProperties;

    /**
     * Construct a new instance.
     *
     */
    public ServerElement(final String name, final String serverGroup) {
        this.name = name;
        this.serverGroup = serverGroup;
    }

    /**
     * Gets whether this server should be started.
     *
     * @return <code>true</code> if the server should be started, <code>false</code> if not
     */
    public boolean isStart() {
        return start;
    }

    /**
     * Sets whether this server should be started.
     *
     * @param start <code>true</code> if the server should be started, <code>false</code> if not
     */
    void setStart(boolean start) {
        this.start = start;
    }

    /**
     * Gets the name of the server.
     *
     * @return the name. Will not be <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the default jvm configuration for servers in this group. This can
     * be overridden at the {@link ServerElement#getJvm() server level}.
     *
     * @return the jvm configuration, or <code>null</code> if there is none
     */
    public JvmElement getJvm() {
        return jvm;
    }

    /**
     * Gets the default jvm configuration for servers in this group.
     *
     * param jvm the jvm configuration. May be <code>null</code>
     */
    void setJvm(JvmElement jvm) {
        this.jvm = jvm;
    }

    public Set<ServerInterfaceElement> getInterfaces() {
        Set<ServerInterfaceElement> intfs = new LinkedHashSet<ServerInterfaceElement>();
        synchronized (interfaces) {
            for (Map.Entry<String, ServerInterfaceElement> entry : interfaces.entrySet()) {
                intfs.add(entry.getValue());
            }
        }
        return intfs;
    }

    public SocketBindingGroupRefElement getSocketBindingGroup() {
        return bindingGroup;
    }

    /**
     * Gets the name of the server's server group.
     *
     * @return the server group name. Will not be <code>null</code>
     */
    public String getServerGroup() {
        return serverGroup;
    }

    /**
     * Gets any system properties defined at the server level. These properties
     * can extend and override any properties declared at the
     * {@link DomainModel#getSystemProperties() domain level}, the
     * {@link ServerGroupElement server group level} or the
     * {@link ServerElement#getSystemProperties() server level}.
     *
     * @return the system properties, or <code>null</code> if there are none
     */
    public PropertiesElement getSystemProperties() {
        return systemProperties;
    }

    /** {@inheritDoc} */
    @Override
    protected Class<ServerElement> getElementClass() {
        return ServerElement.class;
    }

    /** {@inheritDoc} */
    @Override
    public void writeContent(final XMLExtendedStreamWriter streamWriter) throws XMLStreamException {

        // TODO re-evaluate the element order in the xsd; make sure this is correct

        streamWriter.writeAttribute(Attribute.NAME.getLocalName(), name);
        streamWriter.writeAttribute(Attribute.GROUP.getLocalName(), serverGroup);
        if (!start) {
            streamWriter.writeAttribute(Attribute.START.getLocalName(), "false");
        }

        if (! interfaces.isEmpty()) {
            streamWriter.writeStartElement(Element.INTERFACE_SPECS.getLocalName());
            for (ServerInterfaceElement element : interfaces.values()) {
                streamWriter.writeStartElement(Element.INTERFACE.getLocalName());
                element.writeContent(streamWriter);
            }
            streamWriter.writeEndElement();
        }

        if (bindingGroup != null) {
            streamWriter.writeStartElement(Element.SOCKET_BINDING_GROUP.getLocalName());
            bindingGroup.writeContent(streamWriter);
        }

        if (systemProperties != null) {
            streamWriter.writeStartElement(Element.SYSTEM_PROPERTIES.getLocalName());
            systemProperties.writeContent(streamWriter);
            streamWriter.writeEndElement();
        }

        if (jvm != null) {
            streamWriter.writeStartElement(Element.JVM.getLocalName());
            jvm.writeContent(streamWriter);
        }
        streamWriter.writeEndElement();
    }
}

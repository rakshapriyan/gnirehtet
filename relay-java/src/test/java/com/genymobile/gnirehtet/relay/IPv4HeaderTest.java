/*
 * Copyright (C) 2017 Genymobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.genymobile.gnirehtet.relay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.nio.ByteBuffer;

@SuppressWarnings("checkstyle:MagicNumber")
public class IPv4HeaderTest {

    @Test
    public void testReadIPVersionUnavailable() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.flip();
        int firstPacketVersion = IPv4Header.readVersion(buffer);
        Assertions.assertEquals("IPv4 packet version must be unknown", -1, firstPacketVersion);
    }

    @Test
    public void testReadIPVersionAvailable() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        byte versionAndIHL = (4 << 4) | 5;
        buffer.put(versionAndIHL);
        buffer.flip();
        int firstPacketVersion = IPv4Header.readVersion(buffer);
        Assertions.assertEquals("Wrong IP version field value", 4, firstPacketVersion);
    }

    @Test
    public void testReadLengthUnavailable() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.flip();
        int firstPacketLength = IPv4Header.readLength(buffer);
        Assertions.assertEquals("IPv4 packet length must be unknown", -1, firstPacketLength);
    }

    @Test
    public void testReadLengthAvailable() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put(2, (byte) 0x01);
        buffer.put(3, (byte) 0x23);
        buffer.position(20); // consider we wrote the whole header
        buffer.flip();
        int firstPacketLength = IPv4Header.readLength(buffer);
        Assertions.assertEquals("Wrong IP length field value", 0x123, firstPacketLength);
    }

    private static ByteBuffer createMockHeaders() {
        ByteBuffer buffer = ByteBuffer.allocate(28);

        buffer.put((byte) ((4 << 4) | 5)); // versionAndIHL
        buffer.put((byte) 0); // ToS
        buffer.putShort((short) 28); // total length
        buffer.putInt(0); // IdFlagsFragmentOffset
        buffer.put((byte) 0); // TTL
        buffer.put((byte) 17); // protocol (UDP)
        buffer.putShort((short) 0); // checksum
        buffer.putInt(0x12345678); // source address
        buffer.putInt(0x42424242); // destination address

        buffer.limit(28);
        buffer.flip();

        return buffer;
    }

    @Test
    public void testParsePacketHeaders() {
        IPv4Header header = new IPv4Header(createMockHeaders());
        Assertions.assertNotNull(header, "Valid IPv4 header not parsed");
        Assertions.assertTrue(header.isSupported());
        Assertions.assertEquals(IPv4Header.Protocol.UDP, header.getProtocol());
        Assertions.assertEquals(20, header.getHeaderLength());
        Assertions.assertEquals(28, header.getTotalLength());
    }

    @Test
    public void testEditHeaders() {
        ByteBuffer buffer = createMockHeaders();
        IPv4Header header = new IPv4Header(buffer);

        header.setSource(0x87654321);
        header.setDestination(0x24242424);
        header.setTotalLength(42);

        Assertions.assertEquals(0x87654321, header.getSource());
        Assertions.assertEquals(0x24242424, header.getDestination());
        Assertions.assertEquals(42, header.getTotalLength());

        // assert the buffer has been modified
        int source = buffer.getInt(12);
        int destination = buffer.getInt(16);
        int totalLength = Short.toUnsignedInt(buffer.getShort(2));

        Assertions.assertEquals(0x87654321, source);
        Assertions.assertEquals(0x24242424, destination);
        Assertions.assertEquals(42, totalLength);

        header.swapSourceAndDestination();

        Assertions.assertEquals(0x24242424, header.getSource());
        Assertions.assertEquals(0x87654321, header.getDestination());

        source = buffer.getInt(12);
        destination = buffer.getInt(16);

        Assertions.assertEquals(0x24242424, source);
        Assertions.assertEquals(0x87654321, destination);
    }

    @Test
    public void testComputeChecksum() {
        ByteBuffer buffer = createMockHeaders();
        IPv4Header header = new IPv4Header(buffer);

        // set a fake checksum value to assert that it is correctly computed
        buffer.putShort(10, (short) 0x79);

        header.computeChecksum();

        int sum = 0x4500 + 0x001c + 0x0000 + 0x0000 + 0x0011 + 0x0000 + 0x1234 + 0x5678 + 0x4242 + 0x4242;
        while ((sum & ~0xffff) != 0) {
            sum = (sum & 0xffff) + (sum >> 16);
        }
        short checksum = (short) ~sum;

        Assertions.assertEquals(checksum, header.getChecksum());
    }

    @Disabled // manual benchmark
    @Test
    public void benchComputeChecksum() {
        ByteBuffer buffer = createMockHeaders();
        IPv4Header header = new IPv4Header(buffer);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 5000000; ++i) {
            header.computeChecksum();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("5000000 IP checksums: " + duration + "ms");
    }
}
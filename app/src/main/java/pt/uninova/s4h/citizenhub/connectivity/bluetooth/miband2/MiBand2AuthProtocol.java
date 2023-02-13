package pt.uninova.s4h.citizenhub.connectivity.bluetooth.miband2;

import org.hl7.fhir.r4.model.Base;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.connectivity.Protocol;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseCharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BaseDescriptorListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothAgent;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnection;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothProtocol;

public class MiBand2AuthProtocol extends BluetoothProtocol {

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.miband2.authentication");

    private final SecureRandom keyGenerator;

    private byte[] key;

    protected MiBand2AuthProtocol(BluetoothConnection connection, MiBand2Agent agent) {
        super(ID, connection, agent);

        keyGenerator = new SecureRandom();
        key = null;
    }

    private void authorize() {
        final BluetoothConnection connection = getConnection();
        final boolean sendKey = key == null;

        if (sendKey) {
            generateKey();
        }

        connection.addCharacteristicListener(new BaseCharacteristicListener(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH) {
            @Override
            public void onChange(byte[] value) {
                if (value[0] == 0x10 && value[1] == 0x01 && value[2] == 0x04) {
                    if (sendKey) {
                        sendKey();
                    } else {
                        connection.writeCharacteristic(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, new byte[]{0x02, 0x00, 0x02});
                    }
                } else if (value[0] == 0x10 && value[1] == 0x01 && value[2] == 0x01) {
                    connection.writeCharacteristic(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, new byte[]{0x02, 0x00, 0x02});
                } else if (value[0] == 0x10 && value[1] == 0x02 && value[2] == 0x04) {
                    connection.writeCharacteristic(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, new byte[]{0x02, 0x00});
                } else if (value[0] == 0x10 && value[1] == 0x02 && value[2] == 0x01) {
                    try {
                        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
                        SecretKeySpec secret = new SecretKeySpec(key, "AES");

                        cipher.init(Cipher.ENCRYPT_MODE, secret);

                        byte[] handshake = cipher.doFinal(Arrays.copyOfRange(value, 3, 19));
                        byte[] message = new byte[18];

                        message[0] = 0x03;
                        message[1] = 0x00;

                        System.arraycopy(handshake, 0, message, 2, handshake.length);

                        connection.writeCharacteristic(
                                MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH,
                                MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH,
                                message);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }
                } else if (value[0] == 0x10 && value[1] == 0x03 && value[2] == 0x01) {
                    setState(Protocol.STATE_ENABLED);
                }
            }
        });

        connection.addDescriptorListener(new BaseDescriptorListener(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, BluetoothAgent.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION) {
            @Override
            public void onWrite(byte[] value) {
                connection.removeDescriptorListener(this);
                sendKey();
                //connection.writeCharacteristic(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, new byte[]{0x01, 0x08});
            }
        });


        connection.enableNotifications(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH);
    }

    @Override
    public void enable() {
        getAgent().getSettingsManager().get("auth_key", value -> {
            if (value != null) {
                key = Base64.getDecoder().decode(value);
            }

            authorize();
        });
    }

    public void generateKey() {
        key = new byte[16];
        keyGenerator.nextBytes(key);
        getAgent().getSettingsManager().set("auth_key", Base64.getEncoder().encodeToString(key));
    }

    public void sendKey() {
        final byte[] message = new byte[18];

        message[0] = 0x01;
        message[1] = 0x08;

        System.arraycopy(key, 0, message, 2, key.length);

        getConnection().writeCharacteristic(MiBand2Agent.XIAOMI_MIBAND2_SERVICE_AUTH, MiBand2Agent.XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, message);
    }
}

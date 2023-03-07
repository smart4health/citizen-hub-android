package pt.uninova.s4h.citizenhub.connectivity.bluetooth.miband2;

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
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.CharacteristicListener;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.DescriptorListener;

public class MiBand2AuthenticationProtocol extends BluetoothProtocol {

    final private static UUID XIAOMI_MIBAND2_SERVICE_AUTH = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    final private static UUID XIAOMI_MIBAND2_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");

    final public static UUID ID = AgentOrchestrator.namespaceGenerator().getUUID("bluetooth.miband2.authentication");

    private final SecureRandom keyGenerator;
    private final byte[] key;

    private final DescriptorListener descriptorListener = new BaseDescriptorListener(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, BluetoothAgent.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION) {
        @Override
        public void onWrite(byte[] value) {
            //getConnection().removeDescriptorListener(this);
            sendKey();
        }

        @Override
        public void onWriteFailure(byte[] value, int status) {
            super.onWriteFailure(value, status);
        }
    };

    private final CharacteristicListener characteristicListener = new BaseCharacteristicListener(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH) {
        @Override
        public void onChange(byte[] value) {
            if (value[0] == 0x10 && value[1] == 0x01 && value[2] == 0x01) {
                getConnection().writeCharacteristic(
                        XIAOMI_MIBAND2_SERVICE_AUTH,
                        XIAOMI_MIBAND2_CHARACTERISTIC_AUTH,
                        new byte[]{0x02, 0x08});
            } else if (value[0] == 0x10 && value[1] == 0x02 && value[2] == 0x01) {
                try {
                    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
                    SecretKeySpec secret = new SecretKeySpec(key, "AES");

                    cipher.init(Cipher.ENCRYPT_MODE, secret);

                    byte[] handshake = cipher.doFinal(Arrays.copyOfRange(value, 3, 19));
                    byte[] message = new byte[18];

                    message[0] = 0x03;
                    message[1] = 0x08;

                    System.arraycopy(handshake, 0, message, 2, handshake.length);

                    getConnection().writeCharacteristic(
                            XIAOMI_MIBAND2_SERVICE_AUTH,
                            XIAOMI_MIBAND2_CHARACTERISTIC_AUTH,
                            message);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            } else if (value[0] == 0x10 && value[1] == 0x03 && value[2] == 0x01) {
                setState(Protocol.STATE_ENABLED);
            }
        }
    };

    public MiBand2AuthenticationProtocol(BluetoothConnection connection, MiBand2Agent agent) {
        super(ID, connection, agent);

        keyGenerator = new SecureRandom();
        key = new byte[16];
    }

    private void attachObservers() {
        final BluetoothConnection connection = getConnection();

        connection.addCharacteristicListener(characteristicListener);
        connection.addDescriptorListener(descriptorListener);
    }

    private void detachObservers() {
        getConnection().removeCharacteristicListener(characteristicListener);
        getConnection().removeDescriptorListener(descriptorListener);
    }

    @Override
    public void disable() {
        getConnection().disableNotifications(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH);
        detachObservers();
        setState(Protocol.STATE_DISABLED);
    }

    @Override
    public void enable() {
        setState(Protocol.STATE_ENABLING);
        attachObservers();
        getConnection().enableNotifications(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH);
    }

    public void sendKey() {
        getAgent().getSettingsManager().get("auth_key", value -> {
            if (value != null) {
                System.arraycopy(Base64.getDecoder().decode(value), 0, key, 0, key.length);

                getConnection().writeCharacteristic(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, new byte[]{0x02, 0x08});
            } else {
                keyGenerator.nextBytes(key);

                byte[] message = new byte[18];

                message[0] = 0x01;
                message[1] = 0x08;

                System.arraycopy(key, 0, message, 2, message.length - 2);

                getAgent().getSettingsManager().set("auth_key", Base64.getEncoder().encodeToString(key));

                getConnection().writeCharacteristic(XIAOMI_MIBAND2_SERVICE_AUTH, XIAOMI_MIBAND2_CHARACTERISTIC_AUTH, message);
            }
        });
    }
}
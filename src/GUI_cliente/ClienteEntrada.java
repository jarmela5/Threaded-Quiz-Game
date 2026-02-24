package GUI_cliente;

import GameState.GameState;
import TUI_servidor.*;
import perguntas.Question;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClienteEntrada {

    String ip;
     String portStr;
     String codigoJogo;
     String codigoEquipa;
     String nomeUtilizador;


    public ClienteEntrada(String ip, String portStr, String codigoJogo, String codigoEquipa, String nomeUtilizador ) throws IOException, ClassNotFoundException {

        this.ip = ip;
        this.portStr = portStr;
        this.codigoJogo = codigoJogo;
        this.codigoEquipa = codigoEquipa;
        this.nomeUtilizador = nomeUtilizador;

        System.out.println("Entrada do cliente: " + ip + ":" + portStr + " " + codigoJogo + " " + codigoEquipa + " " + nomeUtilizador);



        int port = Integer.parseInt(portStr);

        Socket socket = new Socket(ip, port);


        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        //Envia dados do cliente como String[]
        String[] dadosCliente = {codigoJogo, codigoEquipa, nomeUtilizador};
        out.writeObject(dadosCliente);

        //Recebe resposta do servidor
        Object resposta = in.readObject();
        System.out.println("Servidor: " + resposta);

        if(resposta instanceof String msg){
            System.out.println(msg);
            if (!resposta.equals("OK")) {
                System.out.println("Não foi possível entrar no jogo.");
                socket.close();
                return;
            }
        }




        // Passa os dados recebidos para a GUI
        new GUI_cliente_responder( codigoEquipa, nomeUtilizador, in, out);


    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String ip = args[0];
        String portStr = args[1];
        String codigoJogo = args[2];
        String codigoEquipa = args[3];
        String nomeUtilizador = args[4];

        new ClienteEntrada(ip,portStr,codigoJogo,codigoEquipa,nomeUtilizador);
    }



}



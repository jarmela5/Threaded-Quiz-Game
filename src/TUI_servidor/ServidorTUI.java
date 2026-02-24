package TUI_servidor;

import java.util.Scanner;

import GameState.*;

public class ServidorTUI implements Runnable {

    private final Servidor servidor;
    private final Scanner scanner = new Scanner(System.in);

    public ServidorTUI(Servidor servidor) {
        this.servidor = servidor;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("\n=== Menu do Servidor IsKahoot ===");
            System.out.println("1. Criar nova sala");
            System.out.println("2. Listar salas");
            System.out.println("3. Remover Sala");
            System.out.println("4. Sair");
            System.out.print("Opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> criarSala();
                case "2" -> listarSalas();
                case "3" ->  removerSalas();
                case "4" -> {
                    System.out.println("A encerrar servidor...");
                    servidor.shutdown();
                    return; }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void criarSala() {
        System.out.print("Número de equipas: ");
        int numEquipas = Integer.parseInt(scanner.nextLine());
        System.out.print("Número de perguntas: ");
        int numPerguntas = Integer.parseInt(scanner.nextLine());
        System.out.print("Número de jogadores por equipe: ");
        int numjogadores = Integer.parseInt(scanner.nextLine());
        servidor.criarSala(numEquipas, numPerguntas,numjogadores);
    }

    private void listarSalas() {
        servidor.listarSalas().forEach(System.out::println);
    }


    private void removerSalas(){
        System.out.print("Código da sala: ");
        String codigo = scanner.nextLine();
        servidor.removerSala(codigo);
    }
}

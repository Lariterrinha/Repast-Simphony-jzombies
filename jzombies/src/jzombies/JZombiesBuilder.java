package jzombies;

import repast.simphony.parameter.Parameters;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;


public class JZombiesBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		
		// nome da network, contexto associado, true:rede direcionado com links dos zumbis infectantes aos infectados
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("infection network", context, true);
		netBuilder.buildNetwork();
		
		// Geralmente o nome do projeto e (deve bater com o id no arquivo context.xml)
		context.setId("jzombies");

		// <Object> sugere uma classe generica
		// Nesses metodos é possivel trabalhar com diferentes objetos na classe
		
		
		// Cria o espaço continuo (50x50)
		ContinuousSpaceFactory spaceFactory =
				ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace <Object> space =
				spaceFactory.createContinuousSpace("space", context,
						new RandomCartesianAdder <Object>() ,
						new repast.simphony.space.continuous.WrapAroundBorders(),
						50 , 50);
		
		// Cria a grade (50x50)
		GridFactory gridFactory = GridFactoryFinder.createGridFactory (null);
		// Correct import : import repast.simphony.space.grid.WrapAroundBorders;
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true , 50, 50));
		
		//Classe de parametros
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		// Adiciona Zumbis
		int zombieCount = params.getInteger("zombie_count");;
		for ( int i = 0; i < zombieCount ; i ++) {
			context.add(new Zombie(space , grid));
		}
		
		//Adiciona Humanos
		int humanCount = params.getInteger("human_count");
		for ( int i = 0; i < humanCount ; i ++) {
			int energy = RandomHelper.nextIntFromTo(4, 10);
			context.add(new Human(space ,grid ,energy));
		}
		
		//Adiciona Matador de zumbi
		int zombie_count = params.getInteger("zombie_killer");
		//int zombie_count = 1;
		for ( int i = 0; i < zombie_count ; i ++) {
			context.add(new ZombieKiller(space ,grid));
		}
		
		// Pega a Localização no ContinousSpace e move os objetos para a grade correspondente
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj ,(int)pt.getX(), (int)pt.getY());
		}
		
		
		return context;
	}

}

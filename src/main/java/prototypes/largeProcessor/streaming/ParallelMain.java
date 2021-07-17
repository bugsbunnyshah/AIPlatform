package prototypes.largeProcessor.streaming;

import akka.NotUsed;
import akka.stream.FlowShape;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Balance;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;

public class ParallelMain {
    public static void main(String[] args) throws Exception{
        Flow<ScoopOfBatter, HalfCookedPancake, NotUsed> fryingPan1 =
                Flow.of(ScoopOfBatter.class).map(batter -> new HalfCookedPancake());

        Flow<HalfCookedPancake, Pancake, NotUsed> fryingPan2 =
                Flow.of(HalfCookedPancake.class).map(halfCooked -> new Pancake());

        // With the two frying pans we can fully cook pancakes
        Flow<ScoopOfBatter, Pancake, NotUsed> pancakeChef = fryingPan1.async().via(fryingPan2.async());



        Flow<ScoopOfBatter, Pancake, NotUsed> fryingPan =
                Flow.of(ScoopOfBatter.class).map(batter -> new Pancake());

        Flow<ScoopOfBatter, Pancake, NotUsed> pancakeChef2 =
                Flow.fromGraph(
                        GraphDSL.create(
                                b -> {
                                    final UniformFanInShape<Pancake, Pancake> mergePancakes = b.add(Merge.create(2));
                                    final UniformFanOutShape<ScoopOfBatter, ScoopOfBatter> dispatchBatter =
                                            b.add(Balance.create(2));

                                    // Using two frying pans in parallel, both fully cooking a pancake from the
                                    // batter.
                                    // We always put the next scoop of batter to the first frying pan that becomes
                                    // available.
                                    b.from(dispatchBatter.out(0))
                                            .via(b.add(fryingPan.async()))
                                            .toInlet(mergePancakes.in(0));
                                    // Notice that we used the "fryingPan" flow without importing it via
                                    // builder.add().
                                    // Flows used this way are auto-imported, which in this case means that the two
                                    // uses of "fryingPan" mean actually different stages in the graph.
                                    b.from(dispatchBatter.out(1))
                                            .via(b.add(fryingPan.async()))
                                            .toInlet(mergePancakes.in(1));

                                    return FlowShape.of(dispatchBatter.in(), mergePancakes.out());
                                }));
        System.out.println(pancakeChef2);
    }
}

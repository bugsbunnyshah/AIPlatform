/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.File;

/**
 * Example: training an embedding using the center loss model, on MNIST
 * The motivation is to use the class labels to learn embeddings that have the following properties:
 * (a) Intra-class similarity (i.e., similar vectors for same numbers)
 * (b) Inter-class dissimilarity (i.e., different vectors for different numbers)
 *
 * Refer to the paper "A Discriminative Feature Learning Approach for Deep Face Recognition", Wen et al. (2016)
 * http://ydwen.github.io/papers/WenECCV16.pdf
 *
 * This
 *
 * @author Alex Black
 */
public class AIModel {
    public static void main(String[] args) throws Exception {
        //Save the model
        File locationToSave = new File("MyMultiLayerNetwork.zip");
        //Where to save the network. Note: the file is in .zip format - can be opened externally

        //Load the model
        boolean saveUpdater = true;
        MultiLayerNetwork restored = MultiLayerNetwork.load(locationToSave, saveUpdater);
    }
}

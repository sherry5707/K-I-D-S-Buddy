/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.commons;

import android.widget.ImageView;

/**
 * Callback for implementing images loading in message list
 * modified by Knight.Xu on 2018/4/13.
 * knight.xu add loadLocalImage method
 */
public interface ImageLoader {

    void loadImage(ImageView imageView, String url);

    void loadLocalImage(ImageView imageView, String filePath);

}

class WaterMark {
    //声明私有字段
    #element;
    #text;
    #settings;

    //声明私有字段
    constructor(element, text, settings = {}) {
        this.#element = element || document.documentElement;
        this.#text = text;
        this.#settings = {
            "startX": settings.startX || 20,                     // 水印起始位置x轴坐标
            "startY": settings.startY || 20,                     // 水印起始位置y轴坐标
            "spacingX": settings.spacingX || 150,                // 水印x轴间隔
            "spacingY": settings.spacingY || 100,                // 水印y轴间隔
            "transparency": settings.transparency || 0.15,        // 水印透明度
            "width": settings.width || 200,                // 水印宽度
            "height": settings.height || 100,               // 水印长度
            "rotate_angle": settings.rotate_angle || 20           // 水印倾斜度
        };
    }

    //创建水印
    create(remarkTime = false) {
        window.onload = () => {
            this.#buildWaterMark(remarkTime);
        };
        window.onresize = () => {
            this.#buildWaterMark(remarkTime);
        };
    }

    // 私有方法：生成水印
    #buildWaterMark(remarkTime = false) {
        // 清除已有的水印
        let watermarks = this.#element.getElementsByClassName("watermark");
        for (let watermark of watermarks) {
            watermark.remove();
        }

        // 获取当前页面的宽度和高度
        let page_width = this.#element.scrollWidth - this.#settings.startX;
        let page_height = this.#element.scrollHeight - this.#settings.startY;

        // 计算水印的列数和行数
        let cols = parseInt(page_width / (this.#settings.spacingX + this.#settings.width)) + 1;
        let rows = parseInt(page_height / (this.#settings.spacingY + this.#settings.height)) + 1;

        let x;
        let y;
        // 循环生成水印
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < cols; j++) {
                x = this.#settings.startX + (this.#settings.spacingX + this.#settings.width) * j;
                y = this.#settings.startY + (this.#settings.spacingY + this.#settings.height) * i;
                let new_div = document.createElement("div");
                new_div.className = 'watermark';
                new_div.innerHTML = this.#text + (remarkTime ? `<br>${this.#getNowFormatDay()}` : '');
                new_div.style.transform = "rotate(-" + this.#settings.rotate_angle.toString() + "deg)"; // 旋转
                new_div.style.position = "absolute";
                new_div.style.left = x + 'px';
                new_div.style.top = y + 'px';
                new_div.style.overflow = "hidden";
                new_div.style.zIndex = "9999";
                new_div.style.pointerEvents = "none";
                new_div.style.opacity = "0.3";
                new_div.style.fontSize = "22px";
                new_div.style.textAlign = "center";
                new_div.style.color = "grey";
                this.#element.appendChild(new_div);
            }
        }
    }

    // 获取当前时间
    #getNowFormatDay(nowDate = null) {
        let char = "-";
        if (nowDate == null) {
            nowDate = new Date();
        }
        let day = nowDate.getDate();
        let month = nowDate.getMonth() + 1; // 月份从0开始，所以要加1
        let year = nowDate.getFullYear();
        return year + char + this.#completeDate(month) + char + this.#completeDate(day);
    }

    // 补全日期，确保是两位数
    #completeDate(value) {
        return value < 10 ? "0" + value : value;
    }


    updateElement(element) {
        this.#element = element;
    }

    // 更新水印文本
    updateWaterMarkText(newText) {
        this.#text = newText;
    }

    // 更新水印的透明度
    updateWaterMarkTransparency(newTransparency) {
        this.#settings.transparency = newTransparency;
    }

    // 更新水印的倾斜角度
    updateWaterMarkAngle(newAngle) {
        this.#settings.rotate_angle = newAngle;
    }
}
